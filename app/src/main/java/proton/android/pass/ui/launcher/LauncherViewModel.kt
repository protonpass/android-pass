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

/*
 * Copyright (c) 2022 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.ui.launcher

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState.Ready
import me.proton.core.account.domain.entity.isDisabled
import me.proton.core.account.domain.entity.isReady
import me.proton.core.account.domain.entity.isStepNeeded
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountCreateAddressFailed
import me.proton.core.accountmanager.presentation.onAccountCreateAddressNeeded
import me.proton.core.accountmanager.presentation.onAccountDeviceSecretNeeded
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeFailed
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeNeeded
import me.proton.core.accountmanager.presentation.onSessionSecondFactorNeeded
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.onAddAccountResult
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.PlansOrchestrator
import me.proton.core.plan.presentation.onUpgradeResult
import me.proton.core.usersettings.presentation.UserSettingsOrchestrator
import proton.android.pass.biometry.ResetAuthPreferences
import proton.android.pass.commonrust.api.CommonLibraryVersionChecker
import proton.android.pass.data.api.usecases.InitialWorkerLauncher
import proton.android.pass.data.api.usecases.RefreshPlan
import proton.android.pass.inappupdates.api.InAppUpdatesManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList")
class LauncherViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val authOrchestrator: AuthOrchestrator,
    private val plansOrchestrator: PlansOrchestrator,
    private val userSettingsOrchestrator: UserSettingsOrchestrator,
    private val initialWorkerLauncher: InitialWorkerLauncher,
    private val refreshPlan: RefreshPlan,
    private val inAppUpdatesManager: InAppUpdatesManager,
    private val resetUserPreferences: ResetAuthPreferences,
    private val snackbarDispatcher: SnackbarDispatcher,
    userPreferencesRepository: UserPreferencesRepository,
    commonLibraryVersionChecker: CommonLibraryVersionChecker
) : ViewModel() {

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val version = runCatching { commonLibraryVersionChecker.getVersion() }
                    .getOrElse { "Unknown" }
                PassLogger.i(TAG, "Common library version: $version")
            }
        }
    }

    internal val state: StateFlow<LauncherState> = combine(
        accountManager.getAccounts().map { accounts -> getState(accounts) },
        userPreferencesRepository.getThemePreference(),
        ::LauncherState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = runBlocking {
            val themePreference = userPreferencesRepository.getThemePreference().firstOrNull() ?: ThemePreference.System
            LauncherState(AccountState.Processing, themePreference)
        }
    )

    internal fun register(context: ComponentActivity) {
        authOrchestrator.register(context as ActivityResultCaller)
        plansOrchestrator.register(context)
        userSettingsOrchestrator.register(context)

        authOrchestrator.onAddAccountResult { result ->
            viewModelScope.launch {
                if (result == null && getPrimaryUserIdOrNull() == null) {
                    context.finish()
                }
            }
        }

        accountManager.observe(context.lifecycle, Lifecycle.State.CREATED)
            .onAccountTwoPassModeFailed { accountManager.disableAccount(it.userId) }
            .onAccountCreateAddressFailed { accountManager.disableAccount(it.userId) }
            .onSessionSecondFactorNeeded { authOrchestrator.startSecondFactorWorkflow(it) }
            .onAccountTwoPassModeNeeded { authOrchestrator.startTwoPassModeWorkflow(it) }
            .onAccountCreateAddressNeeded { authOrchestrator.startChooseAddressWorkflow(it) }
            .onAccountDeviceSecretNeeded { authOrchestrator.startDeviceSecretWorkflow(it) }
    }

    internal fun onAccountNeeded() = viewModelScope.launch {
        resetUserPreferences()
        initialWorkerLauncher.cancel()
        addAccount()
    }

    internal fun onPrimaryExist(updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        initialWorkerLauncher.start()
        inAppUpdatesManager.checkForUpdates(updateResultLauncher)
    }

    internal fun addAccount() = viewModelScope.launch {
        authOrchestrator.startAddAccountWorkflow()
    }

    internal fun createAccount() = viewModelScope.launch {
        authOrchestrator.startSignupWorkflow()
    }

    internal fun signIn(userId: UserId? = null) = viewModelScope.launch {
        val account = userId?.let { getAccountOrNull(it) }
        PassLogger.i(TAG, "Signing in: $userId")
        authOrchestrator.startLoginWorkflow(account?.username)
    }

    internal fun disable(userId: UserId) = viewModelScope.launch {
        PassLogger.i(TAG, "Disabling account: $userId")
        snackbarDispatcher.reset()
        accountManager.disableAccount(userId)
    }

    internal fun disableAll() = viewModelScope.launch {
        PassLogger.i(TAG, "Disabling all accounts")
        snackbarDispatcher.reset()

        accountManager.getAccounts(Ready).firstOrNull()
            ?.forEach { accountManager.disableAccount(it.userId) }
    }

    internal fun remove(userId: UserId) = viewModelScope.launch {
        PassLogger.i(TAG, "Removing account: $userId")
        accountManager.removeAccount(userId)
    }

    internal fun switch(userId: UserId) = viewModelScope.launch {
        val account = getAccountOrNull(userId) ?: return@launch
        when {
            account.isDisabled() -> signIn(userId)
            account.isReady() -> setAsPrimary(userId)
        }
    }

    private suspend fun setAsPrimary(userId: UserId) {
        PassLogger.i(TAG, "Setting account as primary: $userId")
        accountManager.setAsPrimary(userId)
    }

    internal fun subscription() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let {
            plansOrchestrator.showCurrentPlanWorkflow(it)
        }
    }

    internal fun upgrade() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let { userId ->
            plansOrchestrator
                .onUpgradeResult { result ->
                    if (result != null) {
                        viewModelScope.launch {
                            runCatching { refreshPlan(userId) }
                                .onFailure { e ->
                                    PassLogger.w(TAG, "Failed refreshing plan for $userId")
                                    PassLogger.w(TAG, e)
                                }
                        }
                    }
                }
                .startUpgradeWorkflow(userId)
        }
    }

    internal fun passwordManagement() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let {
            userSettingsOrchestrator.startPasswordManagementWorkflow(it)
        }
    }

    internal fun recoveryEmail() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let {
            userSettingsOrchestrator.startUpdateRecoveryEmailWorkflow(it)
        }
    }

    internal fun securityKeys() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let {
            userSettingsOrchestrator.startSecurityKeysWorkflow(it)
        }
    }

    @Suppress("ReturnCount")
    private fun getState(accounts: List<Account>): AccountState {

        // Check the case where there are either no accounts or all accounts are not ready
        if (accounts.isEmpty()) {
            PassLogger.i(TAG, "On AccountNeeded has no accounts")
            return AccountState.AccountNeeded
        }

        if (accounts.all { it.isDisabled() }) {
            PassLogger.i(TAG, "On AccountNeeded has accounts states: ${accounts.map { it.state }}")
            return AccountState.AccountNeeded
        }

        // Check the case where at least one account is ready
        if (accounts.any { it.isReady() }) {
            accounts.firstOrNull { it.isReady() }?.let {
                PassLogger.i(TAG, "SessionID=${it.sessionId?.id}")
            }
            return AccountState.PrimaryExist
        }

        // Check if we are in the case where an account needs a step
        if (accounts.any { it.isStepNeeded() }) {
            return AccountState.StepNeeded
        }

        // Base case
        return AccountState.Processing
    }

    private suspend fun getAccountOrNull(it: UserId) = accountManager.getAccount(it).firstOrNull()

    private suspend fun getPrimaryUserIdOrNull() = accountManager.getPrimaryUserId().firstOrNull()

    internal fun cancelUpdateListener() {
        inAppUpdatesManager.tearDown()
    }

    internal fun declineUpdate() {
        inAppUpdatesManager.declineUpdate()
    }

    private companion object {

        private const val TAG = "LauncherViewModel"

    }

}


internal data class LauncherState(
    val accountState: AccountState,
    val themePreference: ThemePreference
)
