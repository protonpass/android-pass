/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.credentials.passkeys.creation.presentation

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.domain.entity.UserId
import proton.android.pass.account.api.AccountOrchestrators
import proton.android.pass.account.api.Orchestrator
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.passkeys.StorePasskey
import proton.android.pass.features.credentials.R
import proton.android.pass.features.credentials.shared.passkeys.events.PasskeyCredentialsTelemetryEvent
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.passkeys.api.GeneratePasskey
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
internal class PasskeyCredentialCreationViewModel @Inject constructor(
    needsBiometricAuth: NeedsBiometricAuth,
    userPreferenceRepository: UserPreferencesRepository,
    private val accountManager: AccountManager,
    private val accountOrchestrators: AccountOrchestrators,
    private val generatePasskey: GeneratePasskey,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val storePasskey: StorePasskey,
    private val telemetryManager: TelemetryManager,
    private val toastManager: ToastManager
) : ViewModel() {

    private val closeScreenFlow = MutableStateFlow<Boolean>(value = false)

    private val requestOptionFlow = MutableStateFlow<Option<PasskeyCredentialCreationRequest?>>(
        value = None
    )

    private val themePreferenceFlow = userPreferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    private val eventFlow = MutableStateFlow<PasskeyCredentialCreationStateEvent>(
        value = PasskeyCredentialCreationStateEvent.Idle
    )

    internal val stateFlow: StateFlow<PasskeyCredentialCreationState> = combine(
        closeScreenFlow,
        requestOptionFlow,
        themePreferenceFlow,
        needsBiometricAuth(),
        eventFlow
    ) { shouldCloseScreen, requestOption, themePreference, isBiometricAuthRequired, event ->
        if (shouldCloseScreen) {
            return@combine PasskeyCredentialCreationState.Close
        }

        when (requestOption) {
            None -> PasskeyCredentialCreationState.NotReady
            is Some ->
                requestOption.value
                    ?.let { request ->
                        PasskeyCredentialCreationState.Ready(
                            request = request,
                            themePreference = themePreference,
                            isBiometricAuthRequired = isBiometricAuthRequired,
                            event = event
                        )
                    }
                    ?: PasskeyCredentialCreationState.Close
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PasskeyCredentialCreationState.NotReady
    )

    internal fun onConsumeEvent(event: PasskeyCredentialCreationStateEvent) {
        eventFlow.compareAndSet(event, PasskeyCredentialCreationStateEvent.Idle)
    }

    internal fun onItemSelected(itemUiModel: ItemUiModel) {
        eventFlow.update {
            PasskeyCredentialCreationStateEvent.OnAskForConfirmation(
                itemUiModel = itemUiModel,
                isLoadingState = IsLoadingState.NotLoading
            )
        }
    }

    internal fun onItemSelectionConfirmed(itemUiModel: ItemUiModel, request: PasskeyCredentialCreationRequest) {
        eventFlow.update {
            PasskeyCredentialCreationStateEvent.OnAskForConfirmation(
                itemUiModel = itemUiModel,
                isLoadingState = IsLoadingState.Loading
            )
        }

        viewModelScope.launch {
            runCatching {
                generatePasskey(
                    url = request.domain,
                    request = request.requestJson
                ).also { generatedPasskey ->
                    storePasskey(
                        shareId = itemUiModel.shareId,
                        itemId = itemUiModel.id,
                        passkey = generatedPasskey.passkey
                    )
                }
            }.onFailure { error ->
                PassLogger.w(TAG, "Error generating passkey from Passkey creation credential")
                PassLogger.w(TAG, error)

                snackbarDispatcher(PasskeyCredentialCreationMessage.PasskeyGenerationError)
                eventFlow.update { PasskeyCredentialCreationStateEvent.Idle }
            }.onSuccess { generatedPasskey ->
                eventFlow.update {
                    PasskeyCredentialCreationStateEvent.OnSendResponse(
                        response = generatedPasskey.response
                    )
                }
            }
        }
    }

    internal fun onRegister(context: ComponentActivity) {
        accountOrchestrators.register(context, listOf(Orchestrator.PlansOrchestrator))
    }

    internal fun onUpdateRequest(newRequest: PasskeyCredentialCreationRequest?) {
        requestOptionFlow.update { newRequest.some() }
    }

    internal fun onResponseSent() {
        telemetryManager.sendEvent(PasskeyCredentialsTelemetryEvent.CreateDone)
    }

    internal fun onUpgrade() {
        viewModelScope.launch {
            accountOrchestrators.start(Orchestrator.PlansOrchestrator)
        }
    }

    internal fun onSignOut(userId: UserId) {
        viewModelScope.launch {
            internalSettingsRepository.setMasterPasswordAttemptsCount(userId, 0)
            accountManager.disableAccount(userId)
            toastManager.showToast(R.string.passkey_credential_selection_logged_out)

            accountManager.getAccounts(AccountState.Ready)
                .firstOrNull()
                ?.filterNot { it.userId == userId }
                ?.isNotEmpty()
                ?.also { hasAccountsLeft ->
                    if (!hasAccountsLeft) {
                        closeScreenFlow.update { true }
                    }
                }
        }
    }

    internal companion object {

        private const val TAG = "PasskeyCredentialCreationViewModel"

    }

}
