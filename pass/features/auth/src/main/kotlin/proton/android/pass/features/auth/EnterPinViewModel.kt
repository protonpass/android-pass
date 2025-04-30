/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.biometry.StoreAuthSuccessful
import proton.android.pass.biometry.UnlockMethod
import proton.android.pass.common.api.CommonRegex
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.usecases.CheckPin
import proton.android.pass.features.auth.EnterPinSnackbarMessage.PinTooManyAttemptsDismissError
import proton.android.pass.features.auth.EnterPinSnackbarMessage.PinTooManyAttemptsError
import proton.android.pass.features.auth.EnterPinUiState.NotInitialised
import proton.android.pass.features.auth.PinConstants.MAX_PIN_ATTEMPTS
import proton.android.pass.features.auth.PinConstants.MAX_PIN_LENGTH
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class EnterPinViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val checkPin: CheckPin,
    private val storeAuthSuccessful: StoreAuthSuccessful,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val origin = savedStateHandleProvider.get()
        .get<AuthOrigin>(AuthOriginNavArgId.key)
        ?: AuthOrigin.AUTO_LOCK

    private val isLoading: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val pinErrorState: MutableStateFlow<Option<PinError>> = MutableStateFlow(None)
    private val eventState: MutableStateFlow<EnterPinEvent> =
        MutableStateFlow(EnterPinEvent.Unknown)

    @OptIn(SavedStateHandleSaveableApi::class)
    private var pinMutableState: String by savedStateHandleProvider.get()
        .saveable { mutableStateOf("") }

    internal val pinState: String
        get() = pinMutableState

    internal val state: StateFlow<EnterPinUiState> = combine(
        eventState,
        pinErrorState,
        isLoading,
        accountManager.getPrimaryUserId().filterNotNull(),
        EnterPinUiState::Data
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = NotInitialised
    )

    internal fun onPinChanged(value: String) {
        val sanitisedValue = value.replace(CommonRegex.NON_DIGIT_REGEX, "").take(MAX_PIN_LENGTH)
        pinErrorState.update { None }
        pinMutableState = sanitisedValue
    }

    internal fun onPinSubmit() {
        if (pinState.isEmpty()) {
            pinErrorState.update { PinError.PinEmpty.some() }
            return
        }

        viewModelScope.launch {
            runCatching {
                val isMatch = checkPin(pinState.encodeToByteArray())
                if (isMatch) {
                    storeAuthSuccessful(UnlockMethod.PinOrBiometrics)
                    eventState.update { EnterPinEvent.Success(origin) }
                    pinMutableState = ""
                } else {
                    isLoading.update { IsLoadingState.Loading }
                    delay(WRONG_PIN_DELAY_SECONDS)
                    isLoading.update { IsLoadingState.NotLoading }
                    val attemptsCount = internalSettingsRepository.getPinAttemptsCount().first() + 1
                    internalSettingsRepository.setPinAttemptsCount(attemptsCount)
                    val remainingAttempts = MAX_PIN_ATTEMPTS - attemptsCount
                    if (remainingAttempts <= 0) {
                        PassLogger.w(TAG, "Too many wrong attempts, logging user out")
                        val userId = accountManager.getPrimaryUserId().firstOrNull()
                            ?: throw UserIdNotAvailableError()
                        when (origin) {
                            AuthOrigin.CONFIGURE_PIN_OR_BIOMETRY -> {
                                snackbarDispatcher(PinTooManyAttemptsError)
                                delay(1.seconds)
                                eventState.update { EnterPinEvent.ForceSignOutAllUsers }
                            }

                            AuthOrigin.AUTO_LOCK,
                            AuthOrigin.EXTRA_PASSWORD_CONFIGURE,
                            AuthOrigin.EXTRA_PASSWORD_LOGIN,
                            AuthOrigin.EXTRA_PASSWORD_REMOVE -> {
                                snackbarDispatcher(PinTooManyAttemptsDismissError)
                                eventState.update { EnterPinEvent.ForcePassword(userId) }
                            }
                        }
                    } else {
                        pinMutableState = ""
                        pinErrorState.update { PinError.PinIncorrect(remainingAttempts).some() }
                    }
                }
            }.onFailure { PassLogger.d(TAG, it, "Failed to check pin") }
        }
    }

    private companion object {

        private val WRONG_PIN_DELAY_SECONDS = 2.seconds

        private const val TAG = "EnterPinViewModel"

    }
}

internal sealed interface EnterPinEvent {

    data object ForceSignOutAllUsers : EnterPinEvent

    @JvmInline
    value class ForcePassword(val userId: UserId) : EnterPinEvent

    @JvmInline
    value class Success(val origin: AuthOrigin) : EnterPinEvent

    data object Unknown : EnterPinEvent

}
