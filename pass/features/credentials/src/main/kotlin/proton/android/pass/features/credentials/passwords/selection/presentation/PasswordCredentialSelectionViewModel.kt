/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.features.credentials.passwords.selection.presentation

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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.supportPayment
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.common.api.None
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.domain.ItemContents
import proton.android.pass.features.credentials.R
import proton.android.pass.features.credentials.shared.passwords.events.PasswordCredentialsTelemetryEvent
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
internal class PasswordCredentialSelectionViewModel @Inject constructor(
    userPreferenceRepository: UserPreferencesRepository,
    needsBiometricAuth: NeedsBiometricAuth,
    appConfig: AppConfig,
    private val accountManager: AccountManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val toastManager: ToastManager,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val telemetryManager: TelemetryManager
) : ViewModel() {

    private val closeScreenFlow = MutableStateFlow<Boolean>(value = false)

    private val requestOptionFlow = MutableStateFlow<Option<PasswordCredentialSelectionRequest?>>(
        value = None
    )

    private val themePreferenceFlow: Flow<ThemePreference> = userPreferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    private val eventFlow = MutableStateFlow<PasswordCredentialSelectionStateEvent>(
        value = PasswordCredentialSelectionStateEvent.Idle
    )

    private val paymentStateFlow = combine(
        flowOf(appConfig.flavor.supportPayment()),
        internalSettingsRepository.hasShownReloadAppWarning()
    ) { supportsPayment, hasShownWarning ->
        supportsPayment to !hasShownWarning
    }

    internal val stateFlow: StateFlow<PasswordCredentialSelectionState> = combineN(
        closeScreenFlow,
        requestOptionFlow,
        themePreferenceFlow,
        needsBiometricAuth(),
        eventFlow,
        paymentStateFlow
    ) { shouldCloseScreen, requestOption, themePreference, isBiometricAuthRequired, event, paymentState ->
        val (supportsPayment, canShowWarningReloadApp) = paymentState
        if (shouldCloseScreen) {
            return@combineN PasswordCredentialSelectionState.Close
        }

        when (requestOption) {
            None -> PasswordCredentialSelectionState.NotReady
            is Some ->
                requestOption.value
                    ?.let { request ->
                        PasswordCredentialSelectionState.Ready(
                            themePreference = themePreference,
                            isBiometricAuthRequired = isBiometricAuthRequired,
                            request = request,
                            event = event,
                            supportPayment = supportsPayment,
                            canShowWarningReloadApp = canShowWarningReloadApp
                        )
                    }
                    ?: PasswordCredentialSelectionState.Close
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PasswordCredentialSelectionState.NotReady
    )

    internal fun onUpdateRequest(newRequest: PasswordCredentialSelectionRequest?) {
        requestOptionFlow.update { newRequest.some() }
    }

    internal fun onScreenShown() {
        telemetryManager.sendEvent(PasswordCredentialsTelemetryEvent.DisplayAllPasswords)
    }

    internal fun onEventConsumed(event: PasswordCredentialSelectionStateEvent) {
        eventFlow.compareAndSet(event, PasswordCredentialSelectionStateEvent.Idle)
    }

    internal fun onAuthPerformed(request: PasswordCredentialSelectionRequest) {
        if (request !is PasswordCredentialSelectionRequest.Use) return

        onPasswordCredentialSelected(
            id = request.username,
            encryptedPassword = request.encryptedPassword
        )
    }

    internal fun onItemSelected(itemUiModel: ItemUiModel) {
        val loginItemContents = itemUiModel.contents as? ItemContents.Login ?: run {
            PassLogger.w(TAG, "Received ItemContents are not ItemContents.Login")
            eventFlow.update { PasswordCredentialSelectionStateEvent.Cancel }
            return
        }

        onPasswordCredentialSelected(
            id = loginItemContents.displayValue,
            encryptedPassword = loginItemContents.password.encrypted
        )
    }

    private fun onPasswordCredentialSelected(id: String, encryptedPassword: EncryptedString) {
        val password = encryptionContextProvider.withEncryptionContext {
            decrypt(encryptedPassword)
        }

        if (password.isEmpty()) {
            PassLogger.w(TAG, "Decrypted password is empty, cannot create PasswordCredential")
            viewModelScope.launch {
                toastManager.showToast(R.string.password_credential_selection_empty_password_error)
            }
            eventFlow.update { PasswordCredentialSelectionStateEvent.Cancel }
            return
        }

        eventFlow.update {
            PasswordCredentialSelectionStateEvent.SendCredentialResponse(
                id = id,
                password = password
            )
        }
        telemetryManager.sendEvent(PasswordCredentialsTelemetryEvent.AuthDone)
    }

    internal fun doNotDisplayReloadAppWarningDialog() {
        internalSettingsRepository.setHasShownReloadAppWarning(true)
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
