/*
 * Copyright (c) 2023-2026 Proton AG
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.account.domain.entity.Account
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountDisabled
import me.proton.core.accountmanager.presentation.onAccountReady
import me.proton.core.accountmanager.presentation.onAccountRemoved
import me.proton.core.accountmanager.presentation.onSessionForceLogout
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.ClearUserData
import proton.android.pass.data.api.usecases.ResetAppToDefaults
import proton.android.pass.data.impl.db.DatabaseCleanupHelper
import proton.android.pass.log.api.LogAccountContext
import proton.android.pass.log.api.LogoutLogger
import proton.android.pass.log.api.LogoutReason
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher

class AccountListenerInitializer : Initializer<Unit> {

    @Suppress("LongMethod")
    override fun create(context: Context) {
        val entryPoint: AccountListenerInitializerEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                AccountListenerInitializerEntryPoint::class.java
            )

        val lifecycleProvider = entryPoint.passAppLifecycleProvider()
        val accountManager = entryPoint.accountManager()
        val itemSyncStatusRepository = entryPoint.itemSyncStatusRepository()
        val snackbarDispatcher = entryPoint.snackbarDispatcher()

        accountManager.observe(
            lifecycle = lifecycleProvider.lifecycle,
            minActiveState = Lifecycle.State.CREATED
        ).onAccountDisabled { account ->
            launchInAppLifecycleScope(lifecycleProvider) {
                withContext(LogAccountContext(account.userId)) {
                    PassLogger.i(TAG, "Account disabled : ${account.userId}")
                    performCleanup(account, entryPoint)
                }
            }
        }.onAccountRemoved { account ->
            launchInAppLifecycleScope(lifecycleProvider) {
                withContext(LogAccountContext(account.userId)) {
                    PassLogger.i(TAG, "Account removed : ${account.userId}")
                    performCleanup(account, entryPoint)
                }
            }
        }.onSessionForceLogout {
            LogoutLogger.record(LogoutReason.ServerSessionRejected)
        }.onAccountReady { account ->
            launchInAppLifecycleScope(lifecycleProvider) {
                withContext(LogAccountContext(account.userId)) {
                    PassLogger.i(TAG, "Account ready : ${account.userId}")
                }
            }
        }

        accountManager.onAccountStateChanged(initialState = false)
            .scan(emptyMap<UserId, AccountState>() to false) { (stateMap, _), currentAccount ->
                val updatedMap = stateMap + (currentAccount.userId to currentAccount.state)
                val justBecameReady = stateMap[currentAccount.userId] != AccountState.Ready &&
                    currentAccount.state == AccountState.Ready
                updatedMap to justBecameReady
            }
            .onEach { (_, justBecameReady) ->
                if (justBecameReady) {
                    launchInAppLifecycleScope(lifecycleProvider) {
                        itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
                    }
                }
            }
            .flowWithLifecycle(lifecycleProvider.lifecycle)
            .launchIn(lifecycleProvider.lifecycle.coroutineScope)

        accountManager.getPrimaryUserId()
            .distinctUntilChanged()
            .onEach { snackbarDispatcher.reset() }
            .flowWithLifecycle(lifecycleProvider.lifecycle)
            .launchIn(lifecycleProvider.lifecycle.coroutineScope)
    }

    private fun launchInAppLifecycleScope(lifecycleProvider: PassAppLifecycleProvider, block: suspend () -> Unit) {
        lifecycleProvider.lifecycle.coroutineScope.launch {
            block()
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>?>> = emptyList()

    private suspend fun performCleanup(account: Account, entryPoint: AccountListenerInitializerEntryPoint) {
        val clearUserData = entryPoint.clearUserData()
        val resetApp = entryPoint.resetAppToDefaults()
        val accountManager = entryPoint.accountManager()
        val databaseCleanupHelper = entryPoint.databaseCleanupHelper()

        entryPoint.itemSyncStatusRepository().clear()

        safeRunCatching { clearUserData(account.userId) }
            .onSuccess { PassLogger.i(TAG, "Cleared user data") }
            .onFailure { PassLogger.i(TAG, it, "Error clearing user data") }

        val activeAccounts = accountManager.getAccounts(AccountState.Ready).firstOrNull().orEmpty()
        PassLogger.i(TAG, "Active accounts remaining: ${activeAccounts.size}")

        if (activeAccounts.isEmpty()) {
            PassLogger.i(TAG, "No more active accounts - performing final cleanup")

            safeRunCatching { databaseCleanupHelper.cleanupLastUser() }
                .onSuccess { PassLogger.i(TAG, "Database cleanup completed") }
                .onFailure {
                    PassLogger.w(TAG, "Error during database cleanup")
                    PassLogger.w(TAG, it)
                }

            resetApp()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccountListenerInitializerEntryPoint {
        fun itemSyncStatusRepository(): ItemSyncStatusRepository
        fun passAppLifecycleProvider(): PassAppLifecycleProvider
        fun accountManager(): AccountManager
        fun resetAppToDefaults(): ResetAppToDefaults
        fun clearUserData(): ClearUserData
        fun snackbarDispatcher(): SnackbarDispatcher
        fun databaseCleanupHelper(): DatabaseCleanupHelper
    }

    companion object {
        private const val TAG = "AccountListenerInitializer"
    }
}
