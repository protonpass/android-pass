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

package proton.android.pass.featurepasskeys.create.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Immutable
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.provider.CallingAppInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.api.Orchestrator
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.flatMap
import proton.android.pass.data.api.usecases.passkeys.StorePasskey
import proton.android.pass.featurepasskeys.R
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.passkeys.api.GeneratePasskey
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

data class CreatePasskeyRequest(
    val callingAppInfo: CallingAppInfo,
    val callingRequest: CreatePublicKeyCredentialRequest,
)

@Immutable
sealed interface State {

    @Immutable
    object Idle : State

    @Immutable
    object Close : State

    @Immutable
    @JvmInline
    value class SendResponse(val response: String) : State
}

@Immutable
sealed interface CreatePasskeyAppState {

    @Immutable
    object NotReady : CreatePasskeyAppState

    @Immutable
    object Close : CreatePasskeyAppState

    @Immutable
    data class Ready(
        val theme: ThemePreference,
        val needsAuth: Boolean
    ) : CreatePasskeyAppState
}

@HiltViewModel
class CreatePasskeyActivityViewModel @Inject constructor(
    private val accountOrchestrators: AccountOrchestrators,
    private val accountManager: AccountManager,
    private val storePasskey: StorePasskey,
    private val generatePasskey: GeneratePasskey,
    private val preferenceRepository: UserPreferencesRepository,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val toastManager: ToastManager,
    needsBiometricAuth: NeedsBiometricAuth
) : ViewModel() {

    private val closeScreenFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val themePreferenceState: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    val state: StateFlow<CreatePasskeyAppState> = combine(
        closeScreenFlow,
        themePreferenceState,
        needsBiometricAuth()
    ) { closeScreen, theme, needsAuth ->
        when {
            closeScreen -> CreatePasskeyAppState.Close
            else -> CreatePasskeyAppState.Ready(
                theme = theme,
                needsAuth = needsAuth
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreatePasskeyAppState.NotReady
    )


//    private val itemUiModelFlow: Flow<LoadingResult<List<ItemUiModel>>> = itemFiltersFlow
//        .flatMapLatest {
//            val (filter, selection) = when (it) {
//                is LoadingResult.Error -> {
//                    PassLogger.w(TAG, "Error observing plan")
//                    PassLogger.w(TAG, it.exception)
//                    return@flatMapLatest flowOf(LoadingResult.Error(it.exception))
//                }
//
//                LoadingResult.Loading -> return@flatMapLatest flowOf(LoadingResult.Loading)
//                is LoadingResult.Success -> it.data
//            }
//            observeActiveItems(
//                filter = filter,
//                shareSelection = selection
//            ).asResultWithoutLoading()
//        }
//        .map { itemResult ->
//            itemResult.map { list ->
//                encryptionContextProvider.withEncryptionContext {
//                    list.map { it.toUiModel(this@withEncryptionContext) }
//                }
//            }
//        }
//        .distinctUntilChanged()

    fun register(context: ComponentActivity) {
        accountOrchestrators.register(context, listOf(Orchestrator.PlansOrchestrator))
    }


//    fun onButtonClick() = viewModelScope.launch {
//        val requestValue = request.value() ?: return@launch
//        val origin = requestValue.callingRequest.origin ?: run {
//            PassLogger.w(TAG, "requestValue.callingRequest.origin was null")
//            _state.update { State.Close }
//            return@launch
//        }
//
//        val created = generatePasskey(origin, requestValue.callingRequest.requestJson)
//        val passkey = created.passkey
//        PassLogger.i(TAG, "Created passkey")
//        PassLogger.d(
//            TAG,
//            "rpname=${passkey.rpName} userDisplayName=${passkey.userDisplayName} username=${passkey.userName}"
//        )
//        storePasskey(
//            shareId = ShareId("123"),
//            itemId = ItemId("123"),
//            passkey = passkey
//        )
//        _state.update { State.SendResponse(created.response) }
//    }

    fun onStop() = viewModelScope.launch {
        preferenceRepository.setHasAuthenticated(HasAuthenticated.NotAuthenticated)
    }

    fun signOut() = viewModelScope.launch {
        val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
        if (primaryUserId != null) {
            accountManager.removeAccount(primaryUserId)
            toastManager.showToast(R.string.passkeys_user_logged_out)
        }
        preferenceRepository.clearPreferences()
            .flatMap { internalSettingsRepository.clearSettings() }
            .onSuccess { PassLogger.d(TAG, "Clearing preferences success") }
            .onFailure {
                PassLogger.w(TAG, "Error clearing preferences")
                PassLogger.w(TAG, it)
            }

        closeScreenFlow.update { true }
    }

    companion object {
        private const val TAG = "CreatePasskeyActivityViewModel"
    }
}
