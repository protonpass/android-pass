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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.presentation.observe
import me.proton.core.accountmanager.presentation.onAccountDisabled
import me.proton.core.accountmanager.presentation.onAccountReady
import me.proton.core.accountmanager.presentation.onAccountRemoved
import me.proton.core.accountmanager.presentation.onSessionForceLogout
import proton.android.pass.commonui.api.PassAppLifecycleProvider
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.ClearUserData
import proton.android.pass.data.api.usecases.GetReadyAccounts
import proton.android.pass.data.api.usecases.ObserveAccountStateChanges
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
        val cleanupHandler by lazy {
            AccountCleanupHandler(
                itemSyncStatusRepository = itemSyncStatusRepository,
                clearUserData = entryPoint.clearUserData(),
                getReadyAccounts = entryPoint.getReadyAccounts(),
                resetAppToDefaults = entryPoint.resetAppToDefaults(),
                cleanupLastUser = { entryPoint.databaseCleanupHelper().cleanupLastUser() }
            )
        }

        accountManager.observe(
            lifecycle = lifecycleProvider.lifecycle,
            minActiveState = Lifecycle.State.CREATED
        ).onAccountDisabled { account ->
            launchInAppLifecycleScope(lifecycleProvider) {
                withContext(LogAccountContext(account.userId)) {
                    PassLogger.i(TAG, "Account disabled : ${account.userId}")
                    cleanupHandler(account.userId)
                }
            }
        }.onAccountRemoved { account ->
            launchInAppLifecycleScope(lifecycleProvider) {
                withContext(LogAccountContext(account.userId)) {
                    PassLogger.i(TAG, "Account removed : ${account.userId}")
                    cleanupHandler(account.userId)
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

        entryPoint.observeAccountStateChanges()()
            .justBecameReady()
            .onEach {
                launchInAppLifecycleScope(lifecycleProvider) {
                    itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
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
        fun getReadyAccounts(): GetReadyAccounts
        fun observeAccountStateChanges(): ObserveAccountStateChanges
    }

    companion object {
        private const val TAG = "AccountListenerInitializer"
    }
}
