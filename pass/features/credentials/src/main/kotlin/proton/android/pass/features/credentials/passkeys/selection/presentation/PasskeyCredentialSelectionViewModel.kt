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

package proton.android.pass.features.credentials.passkeys.selection.presentation

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
import proton.android.pass.data.api.usecases.passkeys.GetPasskeyById
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.Passkey
import proton.android.pass.features.credentials.R
import proton.android.pass.features.credentials.shared.passkeys.events.PasskeyCredentialsTelemetryEvent
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.passkeys.api.AuthenticateWithPasskey
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
internal class PasskeyCredentialSelectionViewModel @Inject constructor(
    userPreferenceRepository: UserPreferencesRepository,
    needsBiometricAuth: NeedsBiometricAuth,
    private val accountOrchestrators: AccountOrchestrators,
    private val accountManager: AccountManager,
    private val toastManager: ToastManager,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val authenticateWithPasskey: AuthenticateWithPasskey,
    private val getPasskeyById: GetPasskeyById,
    private val telemetryManager: TelemetryManager
) : ViewModel() {

    private val closeScreenFlow = MutableStateFlow<Boolean>(value = false)

    private val requestOptionFlow = MutableStateFlow<Option<PasskeyCredentialSelectionRequest?>>(
        value = None
    )

    private val eventFlow = MutableStateFlow<PasskeyCredentialSelectionStateEvent>(
        value = PasskeyCredentialSelectionStateEvent.Idle
    )

    private val themePreferenceFlow: Flow<ThemePreference> = userPreferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    internal val stateFlow: StateFlow<PasskeyCredentialSelectionState> = combine(
        closeScreenFlow,
        requestOptionFlow,
        themePreferenceFlow,
        needsBiometricAuth(),
        eventFlow
    ) { shouldCloseScreen, requestOption, themePreference, isBiometricAuthRequired, event ->
        if (shouldCloseScreen) {
            return@combine PasskeyCredentialSelectionState.Close
        }

        when (requestOption) {
            None -> PasskeyCredentialSelectionState.NotReady
            is Some ->
                requestOption.value
                    ?.let { request ->
                        PasskeyCredentialSelectionState.Ready(
                            themePreference = themePreference,
                            isBiometricAuthRequired = isBiometricAuthRequired,
                            request = request,
                            event = event
                        )
                    }
                    ?: PasskeyCredentialSelectionState.Close
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PasskeyCredentialSelectionState.NotReady
    )

    internal fun onConsumeEvent(event: PasskeyCredentialSelectionStateEvent) {
        eventFlow.compareAndSet(event, PasskeyCredentialSelectionStateEvent.Idle)
    }

    internal fun onUpdateRequest(newRequest: PasskeyCredentialSelectionRequest?) {
        requestOptionFlow.update { newRequest.some() }
    }

    internal fun onUpgrade() {
        viewModelScope.launch {
            accountOrchestrators.start(Orchestrator.PlansOrchestrator)
        }
    }

    internal fun onAuthPerformed(request: PasskeyCredentialSelectionRequest) {
        if (request !is PasskeyCredentialSelectionRequest.Use) return

        viewModelScope.launch {
            when (val passkey = getPasskeyById(request.shareId, request.itemId, request.passkeyId)) {
                None -> {
                    PassLogger.w(TAG, "Passkey not found")
                    eventFlow.update { PasskeyCredentialSelectionStateEvent.Cancel }
                }

                is Some -> {
                    onPasskeySelected(
                        origin = request.requestOrigin,
                        passkey = passkey.value,
                        request = request.requestJson,
                        clientDataHash = request.clientDataHash
                    )
                }
            }
        }
    }

    internal fun onItemSelected(
        itemUiModel: ItemUiModel,
        origin: String,
        request: String,
        clientDataHash: ByteArray?
    ) {
        val itemContents = itemUiModel.contents as? ItemContents.Login ?: run {
            PassLogger.w(TAG, "Received ItemContents that are not ItemContents.Login")
            eventFlow.update { PasskeyCredentialSelectionStateEvent.Cancel }
            return
        }

        when {
            itemContents.hasSinglePasskey -> {
                onPasskeySelected(
                    passkey = itemContents.passkeys.first(),
                    request = request,
                    origin = origin,
                    clientDataHash = clientDataHash
                )
            }

            itemContents.hasPasskeys -> {
                eventFlow.update {
                    PasskeyCredentialSelectionStateEvent.SelectPasskeyFromItem(
                        item = itemUiModel,
                        isLoadingState = IsLoadingState.NotLoading
                    )
                }
            }

            else -> {
                PassLogger.w(TAG, "Received ItemContents with no passkeys")
                eventFlow.update { PasskeyCredentialSelectionStateEvent.Cancel }
            }
        }
    }

    internal fun onPasskeySelected(
        origin: String,
        passkey: Passkey,
        request: String,
        clientDataHash: ByteArray?
    ) {
        runCatching {
            authenticateWithPasskey(
                origin = origin,
                passkey = passkey,
                requestJson = request,
                clientDataHash = clientDataHash
            )
        }.onSuccess { response ->
            PassLogger.i(TAG, "Successfully authenticated with passkey")
            eventFlow.update { PasskeyCredentialSelectionStateEvent.SendResponse(response.response) }
        }.onFailure {
            PassLogger.w(TAG, "Error authenticating with passkey")
            PassLogger.w(TAG, it)
            eventFlow.update { PasskeyCredentialSelectionStateEvent.Cancel }
        }
    }

    internal fun onScreenShown() {
        telemetryManager.sendEvent(PasskeyCredentialsTelemetryEvent.DisplayAllPasskeys)
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

    private companion object {

        private const val TAG = "PasskeyCredentialSelectionViewModel"

    }

}
