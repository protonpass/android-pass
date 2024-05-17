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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
import proton.android.pass.biometry.StoreAuthSuccessful
import proton.android.pass.commonrust.api.CommonLibraryVersionChecker
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.RefreshPlan
import proton.android.pass.data.api.usecases.UserPlanWorkerLauncher
import proton.android.pass.data.api.usecases.organization.RefreshOrganizationSettings
import proton.android.pass.inappupdates.api.InAppUpdatesManager
import proton.android.pass.log.api.PassLogger
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
    private val userPlanWorkerLauncher: UserPlanWorkerLauncher,
    private val itemSyncStatusRepository: ItemSyncStatusRepository,
    private val refreshPlan: RefreshPlan,
    private val inAppUpdatesManager: InAppUpdatesManager,
    private val refreshOrganizationSettings: RefreshOrganizationSettings,
    private val storeAuthSuccessful: StoreAuthSuccessful,
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

        viewModelScope.launch { refreshPlan() }
        viewModelScope.launch { refreshOrganizationSettings() }
    }

    internal val state: StateFlow<State> = accountManager.getAccounts()
        .map { accounts ->
            when {
                accounts.isEmpty() || accounts.all { it.isDisabled() } -> {
                    State.AccountNeeded
                }

                accounts.any { it.isReady() } -> {
                    accounts.firstOrNull { it.isReady() }?.let {
                        PassLogger.i(TAG, "SessionID=${it.sessionId?.id}")
                    }
                    State.PrimaryExist
                }

                accounts.any { it.isStepNeeded() } -> State.StepNeeded
                else -> State.Processing
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = State.Processing
        )

    internal fun register(context: ComponentActivity) {
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
                    accountManager.getAccounts()
                        .first()
                        .size
                        .let { accountCount ->
                            if (accountCount > 1) {
                                itemSyncStatusRepository.tryEmit(ItemSyncStatus.SyncStarted)
                            }
                            itemSyncStatusRepository.observeSyncStatus().first()
                        }
                        .let { itemSyncStatus ->
                            when (itemSyncStatus) {
                                ItemSyncStatus.SyncError,
                                ItemSyncStatus.SyncStarted,
                                ItemSyncStatus.SyncNotStarted,
                                is ItemSyncStatus.Syncing -> SyncMode.ShownToUser

                                ItemSyncStatus.SyncSuccess -> SyncMode.Background
                            }
                        }
                        .also { syncMode ->
                            itemSyncStatusRepository.setMode(syncMode)
                        }

                    refreshOrganizationSettings()
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

    internal fun onUserStateChanced(state: State) = when (state) {
        State.AccountNeeded -> {
            storeAuthSuccessful(resetAttempts = false)
            userPlanWorkerLauncher.cancel()
        }

        State.PrimaryExist -> userPlanWorkerLauncher.start()
        State.Processing,
        State.StepNeeded -> {
            // no-op
        }
    }

    internal fun addAccount() = viewModelScope.launch {
        authOrchestrator.startAddAccountWorkflow(
            requiredAccountType = requiredAccountType,
            creatableAccountType = requiredAccountType,
            product = product
        )
    }

    private fun signIn(userId: UserId? = null) = viewModelScope.launch {
        val account = userId?.let { getAccountOrNull(it) }
        authOrchestrator.startLoginWorkflow(requiredAccountType, username = account?.username)
    }

    internal fun disable(userId: UserId? = null) = viewModelScope.launch {
        accountManager.disableAccount(requireNotNull(userId ?: getPrimaryUserIdOrNull()))
    }

    internal fun remove(userId: UserId? = null) = viewModelScope.launch {
        accountManager.removeAccount(requireNotNull(userId ?: getPrimaryUserIdOrNull()))
    }

    internal fun switch(userId: UserId) = viewModelScope.launch {
        val account = getAccountOrNull(userId) ?: return@launch
        when {
            account.isDisabled() -> signIn(userId)
            account.isReady() -> accountManager.setAsPrimary(userId)
        }
    }

    internal fun subscription() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let {
            plansOrchestrator.showCurrentPlanWorkflow(it)
        }
    }

    internal fun upgrade() = viewModelScope.launch {
        getPrimaryUserIdOrNull()?.let {
            plansOrchestrator
                .onUpgradeResult { result ->
                    if (result != null) {
                        viewModelScope.launch {
                            runCatching { refreshPlan() }
                                .onFailure { e ->
                                    PassLogger.w(TAG, "Failed refreshing plan")
                                    PassLogger.w(TAG, e)
                                }
                        }
                    }
                }
                .startUpgradeWorkflow(it)
        }
    }

    internal fun report() = viewModelScope.launch {
        reportOrchestrator.startBugReport()
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

    private suspend fun getAccountOrNull(it: UserId) = accountManager.getAccount(it).firstOrNull()

    private suspend fun getPrimaryUserIdOrNull() = accountManager.getPrimaryUserId().firstOrNull()

    internal fun checkForUpdates(updateResultLauncher: ActivityResultLauncher<IntentSenderRequest>) {
        inAppUpdatesManager.checkForUpdates(updateResultLauncher)
    }

    internal fun cancelUpdateListener() {
        inAppUpdatesManager.tearDown()
    }

    internal fun declineUpdate() {
        inAppUpdatesManager.declineUpdate()
    }

    internal enum class State {
        Processing,
        AccountNeeded,
        PrimaryExist,
        StepNeeded
    }

    private companion object {

        private const val TAG = "LauncherViewModel"

    }

}
