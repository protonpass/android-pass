/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.extrapassword.auth.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.TooManyExtraPasswordAttemptsException
import proton.android.pass.data.api.errors.WrongExtraPasswordException
import proton.android.pass.data.api.usecases.ObservePrimaryUserEmail
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPassword
import proton.android.pass.data.api.usecases.extrapassword.RemoveExtraPassword
import proton.android.pass.features.extrapassword.auth.navigation.ExtraPasswordOrigin
import proton.android.pass.features.extrapassword.auth.navigation.ExtraPasswordOriginNavArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.UserIdNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class EnterExtraPasswordViewModel @Inject constructor(
    private val authWithExtraPassword: AuthWithExtraPassword,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val removeExtraPassword: RemoveExtraPassword,
    observePrimaryUserEmail: ObservePrimaryUserEmail,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val userId: UserId = savedStateHandleProvider.get()
        .require<String>(UserIdNavArgId.key)
        .let(::UserId)
    private val origin: ExtraPasswordOrigin = savedStateHandleProvider.get()
        .require(ExtraPasswordOriginNavArgId.key)

    private val eventFlow: MutableStateFlow<EnterExtraPasswordEvent> =
        MutableStateFlow(EnterExtraPasswordEvent.Idle)

    private val loadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val errorFlow: MutableStateFlow<Option<ExtraPasswordError>> = MutableStateFlow(None)

    @OptIn(SavedStateHandleSaveableApi::class)
    internal var extraPasswordState: String by savedStateHandleProvider.get()
        .saveable { mutableStateOf("") }

    internal val state: StateFlow<ExtraPasswordState> = combine(
        observePrimaryUserEmail(userId),
        eventFlow,
        loadingFlow,
        errorFlow,
        ::ExtraPasswordState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ExtraPasswordState.Initial
    )

    internal fun onExtraPasswordChanged(value: String) {
        extraPasswordState = value
        errorFlow.update { None }
    }

    internal fun onSubmit() = viewModelScope.launch {
        if (extraPasswordState.isEmpty()) {
            errorFlow.update { ExtraPasswordError.EmptyPassword.some() }
            return@launch
        }

        errorFlow.update { None }

        loadingFlow.update { IsLoadingState.Loading }
        val encryptedPassword = encryptionContextProvider.withEncryptionContext {
            encrypt(extraPasswordState)
        }
        runCatching { authWithExtraPassword(userId, encryptedPassword) }
            .onSuccess {
                PassLogger.i(TAG, "Extra password success")
                when (origin) {
                    ExtraPasswordOrigin.RemoveExtraPassword -> runCatching { removeExtraPassword() }
                        .onSuccess {
                            PassLogger.i(TAG, "Removed extra password successfully")
                            eventFlow.update { EnterExtraPasswordEvent.Success }
                        }
                        .onFailure { err ->
                            PassLogger.w(TAG, "Error removing extra password")
                            PassLogger.w(TAG, err)
                            snackbarDispatcher(EnterExtraPasswordSnackbarMessage.ExtraPasswordError)
                        }

                    ExtraPasswordOrigin.Login -> eventFlow.update { EnterExtraPasswordEvent.Success }
                }
            }
            .onFailure { err ->
                when (err) {
                    is TooManyExtraPasswordAttemptsException -> {
                        PassLogger.w(TAG, "Too many attempts")
                        eventFlow.update { EnterExtraPasswordEvent.Logout(userId) }
                    }

                    is WrongExtraPasswordException -> {
                        PassLogger.w(TAG, "Wrong extra password")
                        errorFlow.update { ExtraPasswordError.WrongPassword.some() }
                    }

                    else -> {
                        PassLogger.w(TAG, "Error performing authentication")
                        PassLogger.w(TAG, err)
                        snackbarDispatcher(EnterExtraPasswordSnackbarMessage.ExtraPasswordError)
                    }
                }
            }
        loadingFlow.update { IsLoadingState.NotLoading }
    }

    internal fun consumeEvent(event: EnterExtraPasswordEvent) {
        eventFlow.compareAndSet(event, EnterExtraPasswordEvent.Idle)
    }

    companion object {
        private const val TAG = "EnterExtraPasswordViewModel"
    }

}
