/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.featureprofile.impl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.extension.getDisplayName
import me.proton.core.user.domain.extension.getEmail
import me.proton.core.user.domain.extension.getInitials
import me.proton.core.util.kotlin.takeIfNotBlank
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.composecomponents.impl.bottombar.AccountType
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.api.usecases.organization.ObserveAnyAccountHasEnforcedLock
import proton.android.pass.data.api.usecases.securelink.ObserveSecureLinksCount
import proton.android.pass.domain.PlanType
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.AppVersionCopied
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.FilteredByAliases
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.FilteredByCreditCards
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.FilteredByIdentities
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.FilteredByLogins
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.FilteredByLoginsWithMFA
import proton.android.pass.featureprofile.impl.ProfileSnackbarMessage.FilteredByNote
import proton.android.pass.featureprofile.impl.accountswitcher.AccountItem
import proton.android.pass.featureprofile.impl.accountswitcher.AccountListItem
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.passkeys.api.CheckPasskeySupport
import proton.android.pass.preferences.AppLockTypePreference
import proton.android.pass.preferences.BiometricSystemLockPreference
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.searchoptions.api.FilterOption
import proton.android.pass.searchoptions.api.HomeSearchOptionsRepository
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.searchoptions.api.VaultSelectionOption
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val autofillManager: AutofillManager,
    private val clipboardManager: ClipboardManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val appConfig: AppConfig,
    private val checkPasskeySupport: CheckPasskeySupport,
    private val userManager: UserManager,
    private val refreshContent: RefreshContent,
    private val searchOptionsRepository: HomeSearchOptionsRepository,
    private val accountManager: AccountManager,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    observeItemCount: ObserveItemCount,
    observeMFACount: ObserveMFACount,
    observeUpgradeInfo: ObserveUpgradeInfo,
    observeAnyAccountHasEnforcedLock: ObserveAnyAccountHasEnforcedLock,
    observeSecureLinksCount: ObserveSecureLinksCount
) : ViewModel() {

    private val userAppLockSectionStateFlow: Flow<AppLockSectionState> = combine(
        userPreferencesRepository.getAppLockTimePreference(),
        userPreferencesRepository.getAppLockTypePreference(),
        userPreferencesRepository.getBiometricSystemLockPreference()
    ) { time, type, biometricSystemLock ->
        when (type) {
            AppLockTypePreference.Biometrics ->
                UserAppLockSectionState.Biometric(time, biometricSystemLock)

            AppLockTypePreference.Pin -> UserAppLockSectionState.Pin(time)
            AppLockTypePreference.None -> UserAppLockSectionState.None
        }
    }

    private val appLockSectionStateFlow: Flow<AppLockSectionState> =
        observeAnyAccountHasEnforcedLock()
            .flatMapLatest { orgSettings ->
                if (orgSettings is Some && orgSettings.value.isEnforced()) {
                    val seconds = orgSettings.value.secondsToForceLock()
                    combine(
                        userPreferencesRepository.getAppLockTypePreference(),
                        userPreferencesRepository.getBiometricSystemLockPreference()
                    ) { type, biometricSystemLock ->
                        when (type) {
                            AppLockTypePreference.Biometrics ->
                                EnforcedAppLockSectionState.Biometric(seconds, biometricSystemLock)

                            AppLockTypePreference.Pin ->
                                EnforcedAppLockSectionState.Pin(seconds)

                            AppLockTypePreference.None ->
                                EnforcedAppLockSectionState.Password(seconds)
                        }
                    }
                } else {
                    userAppLockSectionStateFlow
                }
            }

    private val autofillStatusFlow: Flow<AutofillSupportedStatus> = autofillManager
        .getAutofillStatus()
        .distinctUntilChanged()

    private val eventFlow: MutableStateFlow<ProfileEvent> = MutableStateFlow(ProfileEvent.Unknown)

    private val upgradeInfoFlow = observeUpgradeInfo().asLoadingResult()

    private val itemSummaryUiStateFlow = combine(
        observeItemCount(itemState = null).asLoadingResult(),
        observeMFACount(),
        upgradeInfoFlow
    ) { itemCountResult, mfaCount, upgradeInfoResult ->
        val itemCount = itemCountResult.getOrNull()
        val upgradeInfo = upgradeInfoResult.getOrNull()
        val isUpgradeAvailable = upgradeInfo?.isUpgradeAvailable ?: false

        val aliasLimit = if (isUpgradeAvailable) {
            upgradeInfo?.plan?.aliasLimit?.limitOrNull()
        } else null

        val mfaLimit = if (isUpgradeAvailable) {
            upgradeInfo?.plan?.totpLimit?.limitOrNull()
        } else null

        ItemSummaryUiState(
            loginCount = itemCount?.login?.toInt() ?: 0,
            notesCount = itemCount?.note?.toInt() ?: 0,
            aliasCount = itemCount?.alias?.toInt() ?: 0,
            creditCardsCount = itemCount?.creditCard?.toInt() ?: 0,
            identityCount = itemCount?.identities?.toInt() ?: 0,
            mfaCount = mfaCount,
            aliasLimit = aliasLimit,
            mfaLimit = mfaLimit
        )
    }

    private val passkeySupportFlow: Flow<ProfilePasskeySupportSection> =
        oneShot<ProfilePasskeySupportSection> {
            val support = checkPasskeySupport()
            ProfilePasskeySupportSection.Show(support)
        }.onStart {
            emit(ProfilePasskeySupportSection.Hide)
        }.distinctUntilChanged()

    private val ffFlow = combine(
        featureFlagsPreferencesRepository[FeatureFlag.SL_ALIASES_SYNC],
        featureFlagsPreferencesRepository[FeatureFlag.ADVANCED_ALIAS_MANAGEMENT_V1],
        ::FeatureFlags
    )

    private data class FeatureFlags(
        val isSimpleLoginAliasesSyncEnabled: Boolean,
        val isAdvancedAliasManagementEnabled: Boolean
    )

    private val accountItemsFlow = accountManager.getAccounts()
        .flatMapLatest { accounts ->
            combine(
                accounts.map { account ->
                    combine(
                        observeUpgradeInfo(account.userId).asLoadingResult(),
                        userManager.observeUser(account.userId)
                    ) { upgradeInfo, user ->
                        val (planInfo, _) = processUpgradeInfo(upgradeInfo)
                        account.getAccountItem(user, planInfo)
                    }
                }
            ) { it.toList() }
        }

    private val primaryAccountFlow = accountManager.getPrimaryAccount()

    private val accountsFlow =
        primaryAccountFlow.combine(accountItemsFlow) { primary, accounts -> primary to accounts }
            .mapLatest { (primary, accounts) ->
                accounts.mapNotNull {
                    when {
                        primary?.userId == it.userId -> AccountListItem.Primary(it)
                        it.state == AccountState.Ready -> AccountListItem.Ready(it)
                        it.state == AccountState.Disabled -> AccountListItem.Disabled(it)
                        else -> null
                    }
                }.toPersistentList()
            }

    internal val onAccountReadyFlow = accountManager.getAccounts()
        .scan(emptyMap<UserId, AccountState>() to emptySet<UserId>()) { (previousStates, _), accounts ->
            val currentStates = accounts.associate { it.userId to it.state }
            val newReadyUserIds = currentStates.filter { (userId, currentState) ->
                previousStates[userId] == AccountState.NotReady && currentState == AccountState.Ready
            }.keys

            currentStates to newReadyUserIds
        }
        .map { (_, readyUserIds) -> readyUserIds }
        .distinctUntilChanged()

    internal val state: StateFlow<ProfileUiState> = combineN(
        appLockSectionStateFlow,
        autofillStatusFlow,
        itemSummaryUiStateFlow,
        upgradeInfoFlow,
        eventFlow,
        passkeySupportFlow,
        ffFlow,
        observeSecureLinksCount(),
        accountsFlow
    ) { appLockSectionState, autofillStatus, itemSummaryUiState, upgradeInfo, event,
        passkey, flags, secureLinksCount, accounts ->

        val (accountType, showUpgradeButton) = processUpgradeInfo(upgradeInfo)
        ProfileUiState(
            appLockSectionState = appLockSectionState,
            autofillStatus = autofillStatus,
            itemSummaryUiState = itemSummaryUiState,
            appVersion = appConfig.versionName,
            accountType = accountType,
            event = event,
            showUpgradeButton = showUpgradeButton,
            passkeySupport = passkey,
            secureLinksCount = secureLinksCount,
            accounts = accounts,
            isSimpleLoginAliasesSyncEnabled = flags.isSimpleLoginAliasesSyncEnabled,
            isAdvancedAliasManagementEnabled = flags.isAdvancedAliasManagementEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ProfileUiState.initial(
            appVersion = appConfig.versionName
        )
    )

    private fun processUpgradeInfo(upgradeInfo: LoadingResult<UpgradeInfo>) = when (upgradeInfo) {
        LoadingResult.Loading -> PlanInfo.Hide to false
        is LoadingResult.Error -> {
            PassLogger.w(TAG, "Error getting upgradeInfo")
            PassLogger.w(TAG, upgradeInfo.exception)
            PlanInfo.Hide to false
        }

        is LoadingResult.Success -> {
            val info = upgradeInfo.data
            when (val plan = info.plan.planType) {
                is PlanType.Free -> PlanInfo.Hide to info.isUpgradeAvailable
                is PlanType.Paid -> PlanInfo.Unlimited(
                    planName = plan.humanReadableName,
                    accountType = AccountType.Unlimited
                ) to false

                is PlanType.Trial -> PlanInfo.Trial to info.isUpgradeAvailable
                is PlanType.Unknown -> PlanInfo.Hide to info.isUpgradeAvailable
            }
        }
    }

    internal fun onToggleAutofill(value: Boolean) {
        if (!value) {
            autofillManager.openAutofillSelector()
        } else {
            autofillManager.disableAutofill()
        }
    }

    internal fun copyAppVersion(appVersion: String) {
        clipboardManager.copyToClipboard(appVersion)

        viewModelScope.launch {
            snackbarDispatcher(AppVersionCopied)
        }
    }

    internal fun onAppVersionLongClick() {
        if (appConfig.flavor is BuildFlavor.Alpha) {
            eventFlow.update { ProfileEvent.OpenFeatureFlags }
        }
    }

    internal fun onEventConsumed(event: ProfileEvent) {
        eventFlow.compareAndSet(event, ProfileEvent.Unknown)
    }

    internal fun onToggleBiometricSystemLock(value: Boolean) {
        BiometricSystemLockPreference.from(value)
            .also(userPreferencesRepository::setBiometricSystemLockPreference)
    }

    internal fun onPinSuccessfullyEntered() {
        eventFlow.update { ProfileEvent.ConfigurePin }
    }

    internal fun onNewAccountReady(newAccountReady: Set<UserId>) {
        viewModelScope.launch {
            newAccountReady.forEach {
                refreshAccount(it)
            }
        }
    }

    private suspend fun ProfileViewModel.refreshAccount(userId: UserId) {
        runCatching { refreshContent(userId) }
            .onSuccess {
                PassLogger.i(TAG, "Sync completed")
            }
            .onFailure { error ->
                PassLogger.w(TAG, "Error performing sync")
                PassLogger.w(TAG, error)
            }
    }

    private fun Account.getAccountItem(user: User?, planInfo: PlanInfo): AccountItem = AccountItem(
        userId = userId,
        initials = user?.getInitials(count = 1) ?: "?",
        name = user?.getDisplayName() ?: this.username?.takeIfNotBlank() ?: "Unknown",
        email = user?.getEmail(),
        planInfo = planInfo,
        state = state
    )

    private suspend fun selectFilters(filterType: SearchFilterType) {
        accountManager.getPrimaryUserId().firstOrNull()?.let {
            searchOptionsRepository.setVaultSelectionOption(it, VaultSelectionOption.AllVaults)
        }
        searchOptionsRepository.setFilterOption(FilterOption(filterType))
    }

    fun onAliasCountClick() {
        viewModelScope.launch {
            selectFilters(SearchFilterType.Alias)
            eventFlow.update { ProfileEvent.HomeAliases }
            snackbarDispatcher(FilteredByAliases)
        }
    }

    fun onCreditCardCountClick() {
        viewModelScope.launch {
            selectFilters(SearchFilterType.CreditCard)
            eventFlow.update { ProfileEvent.HomeCreditCards }
            snackbarDispatcher(FilteredByCreditCards)
        }
    }

    fun onIdentityCountClick() {
        viewModelScope.launch {
            selectFilters(SearchFilterType.Identity)
            eventFlow.update { ProfileEvent.HomeIdentities }
            snackbarDispatcher(FilteredByIdentities)
        }
    }

    fun onLoginCountClick() {
        viewModelScope.launch {
            selectFilters(SearchFilterType.Login)
            eventFlow.update { ProfileEvent.HomeLogins }
            snackbarDispatcher(FilteredByLogins)
        }
    }

    fun onMFACountClick() {
        viewModelScope.launch {
            selectFilters(SearchFilterType.LoginMFA)
            eventFlow.update { ProfileEvent.AllMFA }
            snackbarDispatcher(FilteredByLoginsWithMFA)
        }
    }

    fun onNoteCountClick() {
        viewModelScope.launch {
            selectFilters(SearchFilterType.Note)
            eventFlow.update { ProfileEvent.HomeNotes }
            snackbarDispatcher(FilteredByNote)
        }
    }

    private companion object {

        private const val TAG = "ProfileViewModel"

    }

}
