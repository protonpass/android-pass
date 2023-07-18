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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountType
import me.proton.core.account.domain.entity.isDisabled
import me.proton.core.account.domain.entity.isReady
import me.proton.core.account.domain.entity.isStepNeeded
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountCreateAddressFailed
import me.proton.core.accountmanager.presentation.onAccountCreateAddressNeeded
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeFailed
import me.proton.core.accountmanager.presentation.onAccountTwoPassModeNeeded
import me.proton.core.accountmanager.presentation.onSessionForceLogout
import me.proton.core.accountmanager.presentation.onSessionSecondFactorNeeded
import me.proton.core.auth.presentation.AuthOrchestrator
import me.proton.core.auth.presentation.onAddAccountResult
import me.proton.core.domain.entity.Product
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.presentation.PlansOrchestrator
import me.proton.core.plan.presentation.onUpgradeResult
import me.proton.core.report.presentation.ReportOrchestrator
import me.proton.core.user.domain.UserManager
import me.proton.core.usersettings.presentation.UserSettingsOrchestrator
import proton.android.pass.autofill.api.AutofillManager
import proton.android.pass.common.api.flatMap
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.usecases.ClearUserData
import proton.android.pass.data.api.usecases.RefreshPlan
import proton.android.pass.data.api.usecases.UserPlanWorkerLauncher
import proton.android.pass.inappupdates.api.InAppUpdatesManager
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.UserPreferencesRepository
import javax.inject.Inject

@HiltViewModel
@Suppress("LongParameterList")
class LauncherViewModel @Inject constructor(
    private val product: Product,
    private val requiredAccountType: AccountType,
    private val accountManager: AccountManager,
    private val userManager: UserManager,
    private val authOrchestrator: AuthOrchestrator,
    private val plansOrchestrator: PlansOrchestrator,
    private val reportOrchestrator: ReportOrchestrator,
    private val userSettingsOrchestrator: UserSettingsOrchestrator,
    private val preferenceRepository: UserPreferencesRepository,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val userPlanWorkerLauncher: UserPlanWorkerLauncher,
    private val itemSyncStatusRepository: ItemSyncStatusRepository,
    private val clearUserData: ClearUserData,
    private val refreshPlan: RefreshPlan,
    private val inAppUpdatesManager: InAppUpdatesManager,
    private val autofillManager: AutofillManager
) : ViewModel() {

    val state: StateFlow<State> = accountManager.getAccounts()
        .map { accounts ->
            when {
                accounts.isEmpty() || accounts.all { it.isDisabled() } -> {
                    clearPassUserData(accounts)
                    State.AccountNeeded
                }

                accounts.any { it.isReady() } -> State.PrimaryExist
                accounts.any { it.isStepNeeded() } -> State.StepNeeded
                else -> State.Processing
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = State.Processing
        )

    fun register(context: ComponentActivity) {
        authOrchestrator.register(context as ActivityResultCaller)
        plansOrchestrator.register(context)
        reportOrchestrator.register(context)
        userSettingsOrchestrator.register(context)

        authOrchestrator.onAddAccountResult { result ->
            viewModelScope.launch {
                if (result == null && getPrimaryUserIdOrNull() == null) {
                    context.finish()
                    return@launch
                }

                if (result != null) {
                    PassLogger.i(TAG, "Sending User Access")
                    itemSyncStatusRepository.emit(ItemSyncStatus.Syncing)
                }
            }
        }

        accountManager.observe(context.lifecycle, Lifecycle.State.CREATED)
            .onSessionForceLogout { userManager.lock(it.userId) }
            .onAccountTwoPassModeFailed { accountManager.disableAccount(it.userId) }
            .onAccountCreateAddressFailed { accountManager.disableAccount(it.userId) }
            .onSessionSecondFactorNeeded { authOrchestrator.startSecondFactorWorkflow(it) }
            .onAccountTwoPassModeNeeded { authOrchestrator.startTwoPassModeWorkflow(it) }
            .onAccountCreateAddressNeeded { authOrchestrator.startChooseAddressWorkflow(it) }
    }

    fun onUserStateChanced(state: State) = viewModelScope.launch {
        when (state) {
            State.AccountNeeded -> userPlanWorkerLauncher.cancel()
            State.PrimaryExist -> userPlanWorkerLauncher.start()
            State.Processing,
            State.StepNeeded -> {
                // no-op
            }
        }
    }

    fun addAccount() = viewModelScope.launch {
        authOrchestrator.startAddAccountWorkflow(
            requiredAccountType = requiredAccountType,
            creatableAccountType = requiredAccountType,
            product = product
        )
    }

    fun signIn(userId: UserId? = null) = viewModelScope.launch {
        val account = userId?.let { getAccountOrNull(it) }
        authOrchestrator.startLoginWorkflow(requiredAccountType, username = account?.username)
    }

    fun signOut(userId: UserId? = null) = viewModelScope.launch {
        accountManager.disableAccount(requireNotNull(userId ?: getPrimaryUserIdOrNull()))
        clearPreferencesIfNeeded()
    }

    fun switch(userId: UserId) = viewModelScope.launch {
        val account = getAccountOrNull(userId) ?: return@launch
        when {
            account.isDisabled() -> signIn(userId)
            account.isReady() -> accountManager.setAsPrimary(userId)
        }
    }

    fun remove(userId: UserId? = null) = viewModelScope.launch {
        accountManager.removeAccount(requireNotNull(userId ?: getPrimaryUserIdOrNull()))
        clearPreferencesIfNeeded()
    }

    private suspend fun clearPreferencesIfNeeded() {
        val accounts = accountManager.getAccounts().first()
        if (accounts.isEmpty()) {
            preferenceRepository.clearPreferences()
                .flatMap { internalSettingsRepository.clearSettings() }
                .onSuccess { PassLogger.d(TAG, "Clearing preferences success") }
                .onFailure {
                    PassLogger.w(TAG, it, "Error clearing preferences")
                }
            autofillManager.disableAutofill()
        }
    }

    fun subscription() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let {
            plansOrchestrator.showCurrentPlanWorkflow(it)
        }
    }

    fun upgrade() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let {
            plansOrchestrator
                .onUpgradeResult { result ->
                    if (result != null) {
                        viewModelScope.launch {
                            runCatching { refreshPlan() }
                                .onFailure { e ->
                                    PassLogger.w(TAG, e, "Failed refreshing plan")
                                }
                        }
                    }
                }
                .startUpgradeWorkflow(it)
        }
    }

    fun report() = viewModelScope.launch {
        reportOrchestrator.startBugReport()
    }

    fun passwordManagement() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let {
            userSettingsOrchestrator.startPasswordManagementWorkflow(it)
        }
    }

    private suspend fun clearPassUserData(accounts: List<Account>) {
        val disabledAccounts = accounts.filter { it.isDisabled() }
        disabledAccounts.forEach { account ->
            PassLogger.i(TAG, "Clearing user data")
            runCatching { clearUserData(account.userId) }
                .onSuccess { PassLogger.i(TAG, "Cleared user data") }
                .onFailure { PassLogger.i(TAG, it, "Error clearing user data") }
        }

        // If there are no accounts left, disable autofill and clear preferences
        val allDisabled = accounts.all { it.isDisabled() }
        if (accounts.isEmpty() || allDisabled) {
            preferenceRepository.clearPreferences()
                .flatMap { internalSettingsRepository.clearSettings() }
                .onSuccess { PassLogger.d(TAG, "Clearing preferences success") }
                .onFailure {
                    PassLogger.w(TAG, it, "Error clearing preferences")
                }

            autofillManager.disableAutofill()
        }
    }

    private suspend fun getAccountOrNull(it: UserId) = accountManager.getAccount(it).firstOrNull()
    private suspend fun getPrimaryUserIdOrNull() = accountManager.getPrimaryUserId().firstOrNull()

    fun checkForUpdates(updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        inAppUpdatesManager.checkForUpdates(updateResultLauncher)
    }

    fun cancelUpdateListener() {
        inAppUpdatesManager.tearDown()
    }

    fun declineUpdate() {
        inAppUpdatesManager.declineUpdate()
    }

    enum class State { Processing, AccountNeeded, PrimaryExist, StepNeeded }

    companion object {
        private const val TAG = "LauncherViewModel"
    }
}
