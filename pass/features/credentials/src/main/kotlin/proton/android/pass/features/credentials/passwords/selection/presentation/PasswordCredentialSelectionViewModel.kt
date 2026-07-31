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
import kotlinx.coroutines.flow.first
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
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.AssetLinkRepository
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.VerifyDigitalAssetLinksForCredentialSharing
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.features.credentials.R
import proton.android.pass.features.credentials.shared.passwords.search.PasswordCallerContext
import proton.android.pass.features.credentials.shared.passwords.search.PasswordOriginResolver
import proton.android.pass.features.credentials.shared.passwords.search.StoredPasswordAppAssociationAuthorizer
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
    private val telemetryManager: TelemetryManager,
    private val assetLinkRepository: AssetLinkRepository,
    private val verifyDigitalAssetLinksForCredentialSharing: VerifyDigitalAssetLinksForCredentialSharing,
    private val getItemById: GetItemById,
    private val itemRepository: ItemRepository,
    private val storedPasswordAppAssociationAuthorizer: StoredPasswordAppAssociationAuthorizer,
    private val passwordOriginResolver: PasswordOriginResolver
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

    private val associationCandidateFlow = MutableStateFlow<ItemUiModel?>(null)

    private val paymentStateFlow = combine(
        flowOf(appConfig.flavor.supportPayment()),
        internalSettingsRepository.hasShownReloadAppWarning()
    ) { supportsPayment, hasShownWarning ->
        supportsPayment to !hasShownWarning
    }

    private val eventAssociationAndPaymentFlow = combine(
        eventFlow,
        associationCandidateFlow,
        paymentStateFlow
    ) { event, candidate, paymentState ->
        Triple(event, candidate, paymentState)
    }

    internal val stateFlow: StateFlow<PasswordCredentialSelectionState> = combine(
        closeScreenFlow,
        requestOptionFlow,
        themePreferenceFlow,
        needsBiometricAuth(),
        eventAssociationAndPaymentFlow
    ) { shouldCloseScreen, requestOption, themePreference, isBiometricAuthRequired,
        (event, associationCandidate, paymentState) ->
        val (supportsPayment, canShowWarningReloadApp) = paymentState
        if (shouldCloseScreen) {
            return@combine PasswordCredentialSelectionState.Close
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
                            associationCandidate = associationCandidate,
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

        val request = (requestOptionFlow.value as? Some)?.value as? PasswordCredentialSelectionRequest.Select ?: run {
            PassLogger.w(TAG, "Received item selection outside of a Select request")
            eventFlow.update { PasswordCredentialSelectionStateEvent.Cancel }
            return
        }

        viewModelScope.launch {
            val storedAssociations = runCatching {
                getItemById(
                    userId = itemUiModel.userId,
                    shareId = itemUiModel.shareId,
                    itemId = itemUiModel.id
                ).packageInfoSet
            }.getOrDefault(emptySet())
            val isAuthorizedForCredentialSharing = isAuthorizedForCredentialSharing(
                callerContext = request.callerContext,
                loginUrls = loginItemContents.urls,
                storedAssociations = storedAssociations
            )

            if (!isAuthorizedForCredentialSharing) {
                if (request.callerContext is PasswordCallerContext.Native && loginItemContents.urls.isEmpty()) {
                    associationCandidateFlow.value = itemUiModel
                    return@launch
                }
                PassLogger.w(TAG, "Selected item is not authorized for credential sharing with calling app")
                eventFlow.update { PasswordCredentialSelectionStateEvent.Cancel }
                return@launch
            }

            onPasswordCredentialSelected(
                id = loginItemContents.displayValue,
                encryptedPassword = loginItemContents.password.encrypted
            )
        }
    }

    internal fun onAssociationConfirmed() {
        val candidate = associationCandidateFlow.value ?: return
        val request = (requestOptionFlow.value as? Some)?.value as? PasswordCredentialSelectionRequest.Select ?: return
        val caller = request.callerContext as? PasswordCallerContext.Native ?: return
        val login = candidate.contents as? ItemContents.Login ?: return

        viewModelScope.launch {
            val result = runCatching {
                itemRepository.addPackageAndUrlToItem(
                    userId = candidate.userId,
                    shareId = candidate.shareId,
                    itemId = candidate.id,
                    packageInfo = PackageInfo(
                        packageName = PackageName(caller.packageName),
                        appName = AppName(caller.packageName),
                        hashes = caller.certificateFingerprints
                    ).some(),
                    url = None
                )
            }
            associationCandidateFlow.value = null
            result.onSuccess {
                onPasswordCredentialSelected(
                    id = login.displayValue,
                    encryptedPassword = login.password.encrypted
                )
            }.onFailure {
                PassLogger.w(TAG, "Unable to save the selected app association")
                eventFlow.value = PasswordCredentialSelectionStateEvent.Cancel
            }
        }
    }

    internal fun onAssociationCancelled() {
        associationCandidateFlow.value = null
        eventFlow.value = PasswordCredentialSelectionStateEvent.Cancel
    }

    private suspend fun isAuthorizedForCredentialSharing(
        callerContext: PasswordCallerContext?,
        loginUrls: List<String>,
        storedAssociations: Set<PackageInfo>
    ): Boolean = when (callerContext) {
        is PasswordCallerContext.Browser -> loginUrls.isEmpty() || loginUrls
            .mapNotNull(passwordOriginResolver::canonicalizeLoginUrl)
            .any { it == callerContext.origin }

        is PasswordCallerContext.Native -> isNativeCallerAuthorized(
            callerContext = callerContext,
            loginUrls = loginUrls,
            storedAssociations = storedAssociations
        )

        null -> false
    }

    private suspend fun isNativeCallerAuthorized(
        callerContext: PasswordCallerContext.Native,
        loginUrls: List<String>,
        storedAssociations: Set<PackageInfo>
    ): Boolean {
        val loginOrigins = loginUrls
            .mapNotNull(passwordOriginResolver::canonicalizeLoginUrl)
            .distinct()

        return storedPasswordAppAssociationAuthorizer(callerContext, storedAssociations) ||
            loginOrigins.takeIf(List<String>::isNotEmpty)?.let { origins ->
                hasCachedAssociation(callerContext.packageName, origins) ||
                    hasLiveDigitalAssetLinkAssociation(callerContext, origins)
            } == true
    }

    private suspend fun hasCachedAssociation(packageName: String, loginOrigins: List<String>): Boolean {
        val cachedOrigins = assetLinkRepository
            .observeByPackageName(packageName)
            .first()
            .mapNotNull { assetLink -> passwordOriginResolver.canonicalizeLoginUrl(assetLink.website) }
            .toSet()

        return loginOrigins.any { it in cachedOrigins }
    }

    private suspend fun hasLiveDigitalAssetLinkAssociation(
        callerContext: PasswordCallerContext.Native,
        loginOrigins: List<String>
    ): Boolean = loginOrigins.any { origin ->
        verifyDigitalAssetLinksForCredentialSharing(
            website = origin,
            packageName = callerContext.packageName,
            certificateFingerprints = callerContext.certificateFingerprints
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
