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
import kotlinx.coroutines.flow.scan
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

        accountManager.observe(
            lifecycle = lifecycleProvider.lifecycle,
            minActiveState = Lifecycle.State.CREATED
        ).onAccountDisabled {
            PassLogger.i(TAG, "Account disabled")
            performCleanup(it, entryPoint)
        }.onAccountRemoved {
            PassLogger.i(TAG, "Account removed")
            performCleanup(it, entryPoint)
        }.onAccountReady(false) { // this flag is set to false to listen for new accounts only
            PassLogger.i(TAG, "New Account ready")
            refreshOrganizationSettings()
        }

        // onAccountReady doesn't notify sometimes when using extra password during login
        accountManager.getAccounts()
            .scan(emptyMap<UserId, AccountState>()) { previousStates, accounts ->
                accounts.associate { it.userId to it.state }.onEach { (userId, currentState) ->
                    val previousState = previousStates[userId]
                    if (previousState == AccountState.NotReady && currentState == AccountState.Ready) {
                        val itemSyncStatus = itemSyncStatusRepository.observeSyncStatus().first()
                        itemSyncStatusRepository.setMode(itemSyncStatus.toSyncMode())
                    }
                }
            }
            .flowWithLifecycle(lifecycleProvider.lifecycle)
            .launchIn(lifecycleProvider.lifecycle.coroutineScope)
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

        val activeAccounts = accountManager.getAccounts(AccountState.Ready).firstOrNull()

        // If there are no more active accounts, reset the app to defaults
        if (activeAccounts?.isEmpty() == true) {
            resetApp()
        }
    }

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AccountListenerInitializerEntryPoint {
        fun itemSyncStatusRepository(): ItemSyncStatusRepository
        fun refreshOrganizationSettings(): RefreshOrganizationSettings
        fun passAppLifecycleProvider(): PassAppLifecycleProvider
        fun accountManager(): AccountManager
        fun resetAppToDefaults(): ResetAppToDefaults
        fun clearUserData(): ClearUserData
    }

    companion object {
        private const val TAG = "AccountListenerInitializer"
    }
}
