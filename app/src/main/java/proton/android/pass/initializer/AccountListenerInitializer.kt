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

package proton.android.pass.initializer

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.flowWithLifecycle
import androidx.startup.Initializer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountDisabled
import me.proton.core.accountmanager.presentation.onAccountReady
import me.proton.core.accountmanager.presentation.onAccountRemoved
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.toSyncMode
import proton.android.pass.data.api.usecases.ClearUserData
import proton.android.pass.data.api.usecases.RefreshPlan
import proton.android.pass.data.api.usecases.ResetAppToDefaults
import proton.android.pass.data.api.usecases.organization.RefreshOrganizationSettings
import proton.android.pass.log.api.PassLogger

class AccountListenerInitializer : Initializer<Unit> {
    override fun create(context: Context) {
        val entryPoint: AccountListenerInitializerEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AccountListenerInitializerEntryPoint::class.java
            )

        val lifecycleProvider = entryPoint.passAppLifecycleProvider()
        val accountManager = entryPoint.accountManager()
        val itemSyncStatusRepository = entryPoint.itemSyncStatusRepository()
        val refreshOrganizationSettings = entryPoint.refreshOrganizationSettings()
        val refreshPlan = entryPoint.refreshPlan()

        accountManager.observe(
            lifecycle = lifecycleProvider.lifecycle,
            minActiveState = Lifecycle.State.CREATED
        ).onAccountDisabled {
            PassLogger.i(TAG, "Account disabled : ${it.userId}")
            launchInAppLifecycleScope(lifecycleProvider) {
                performCleanup(it, entryPoint)
            }
        }.onAccountRemoved {
            PassLogger.i(TAG, "Account removed : ${it.userId}")
            launchInAppLifecycleScope(lifecycleProvider) {
                performCleanup(it, entryPoint)
            }
        }.onAccountReady { account ->
            launchInAppLifecycleScope(lifecycleProvider) {
                onAccountReady(account, refreshOrganizationSettings, refreshPlan)
            }
        }

        accountManager.onAccountStateChanged(initialState = false)
            .scan(mutableMapOf<UserId, Pair<Account?, Account>>()) { stateMap, currentAccount ->
                val userId = currentAccount.userId
                val previousAccount = stateMap[userId]?.second
                stateMap[userId] = Pair(previousAccount, currentAccount)
                stateMap
            }
            .onEach { map ->
                val anyUserNeedsSync = map.any {
                    val previousState = it.value.first?.state
                    val currentState = it.value.second.state
                    previousState == AccountState.NotReady && currentState == AccountState.Ready
                }
                if (anyUserNeedsSync) {
                    launchInAppLifecycleScope(lifecycleProvider) {
                        val itemSyncStatus = itemSyncStatusRepository.observeSyncStatus().first()
                        itemSyncStatusRepository.setMode(itemSyncStatus.toSyncMode())
                    }
                }
            }
            .flowWithLifecycle(lifecycleProvider.lifecycle)
            .launchIn(lifecycleProvider.lifecycle.coroutineScope)
    }

    private fun launchInAppLifecycleScope(lifecycleProvider: PassAppLifecycleProvider, block: suspend () -> Unit) {
        lifecycleProvider.lifecycle.coroutineScope.launch {
            block()
        }
    }

    private suspend fun onAccountReady(
        account: Account,
        refreshOrganizationSettings: RefreshOrganizationSettings,
        refreshPlan: RefreshPlan
    ) {
        PassLogger.i(TAG, "Account ready : ${account.userId}")
        refreshOrganizationSettings(account.userId)
        runCatching {
            refreshPlan(account.userId)
        }.onSuccess {
            PassLogger.i(TAG, "Plan refreshed for ${account.userId}")
        }.onFailure {
            PassLogger.w(TAG, "Refresh plan errored for ${account.userId}")
            PassLogger.w(TAG, it)
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = emptyList()

    private suspend fun performCleanup(account: Account, entryPoint: AccountListenerInitializerEntryPoint) {
        val clearUserData = entryPoint.clearUserData()
        val resetApp = entryPoint.resetAppToDefaults()
        val accountManager = entryPoint.accountManager()

        entryPoint.itemSyncStatusRepository().clear()
        runCatching { clearUserData(account.userId) }
            .onSuccess { PassLogger.i(TAG, "Cleared user data") }
            .onFailure { PassLogger.i(TAG, it, "Error clearing user data") }

        val activeAccounts = accountManager.getAccounts(AccountState.Ready).firstOrNull().orEmpty()
        PassLogger.i(TAG, "Active accounts : ${activeAccounts.size}")
        // If there are no more active accounts, reset the app to defaults
        if (activeAccounts.isEmpty()) {
            resetApp()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccountListenerInitializerEntryPoint {
        fun itemSyncStatusRepository(): ItemSyncStatusRepository
        fun refreshOrganizationSettings(): RefreshOrganizationSettings
        fun refreshPlan(): RefreshPlan
        fun passAppLifecycleProvider(): PassAppLifecycleProvider
        fun accountManager(): AccountManager
        fun resetAppToDefaults(): ResetAppToDefaults
        fun clearUserData(): ClearUserData
    }

    companion object {
        private const val TAG = "AccountListenerInitializer"
    }
}
