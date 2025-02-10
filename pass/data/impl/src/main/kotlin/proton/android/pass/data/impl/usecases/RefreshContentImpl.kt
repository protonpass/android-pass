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

package proton.android.pass.data.impl.usecases

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.RefreshSharesResult
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.RefreshContent
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationVaultsPolicy
import proton.android.pass.data.impl.R
import proton.android.pass.data.impl.work.FetchItemsWorker
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.entity.NewVault
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class RefreshContentImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository,
    private val workManager: WorkManager,
    private val syncStatusRepository: ItemSyncStatusRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val createVault: CreateVault,
    private val observeOrganizationVaultsPolicy: ObserveOrganizationVaultsPolicy
) : RefreshContent {

    override suspend fun invoke(userId: UserId?) {
        PassLogger.i(TAG, "Refreshing shares")
        syncStatusRepository.clear()
        syncStatusRepository.setMode(SyncMode.ShownToUser)
        syncStatusRepository.emit(ItemSyncStatus.SyncStarted)

        val actualUserId = userId ?: accountManager.getPrimaryUserId().firstOrNull()
            ?: throw UserIdNotAvailableError()
        runCatching { shareRepository.refreshShares(actualUserId) }
            .onFailure { error ->
                PassLogger.w(TAG, "Error refreshing shares")
                PassLogger.w(TAG, error)
                syncStatusRepository.emit(ItemSyncStatus.SyncError)
                throw error
            }
            .onSuccess { refreshSharesResult ->
                PassLogger.i(TAG, "Shares for user: $actualUserId refreshed")
                if (refreshSharesResult.allShareIds.isEmpty()) {
                    handleSharesWhenEmpty(actualUserId)
                } else {
                    handleExistingShares(actualUserId, refreshSharesResult)
                }
            }
    }

    private suspend fun handleSharesWhenEmpty(userId: UserId) {
        val canCreateVaults = observeOrganizationVaultsPolicy()
            .first()
            .value()
            ?.canCreateVaults
            ?: true

        if (!canCreateVaults) {
            PassLogger.i(TAG, "Received an empty list of shares, skipping default vault creation")

            syncStatusRepository.setMode(SyncMode.Background)
            syncStatusRepository.emit(ItemSyncStatus.SyncSuccess)
            return
        }

        PassLogger.i(TAG, "Received an empty list of shares, creating default vault")
        runCatching {
            val vault = encryptionContextProvider.withEncryptionContextSuspendable {
                NewVault(
                    name = encrypt(context.getString(R.string.vault_name)),
                    description = encrypt(context.getString(R.string.vault_description)),
                    icon = ShareIcon.Icon1,
                    color = ShareColor.Color1
                )
            }
            createVault(userId, vault)
        }
            .onFailure { error ->
                PassLogger.w(TAG, "Error creating default vault")
                PassLogger.w(TAG, error)
                throw error
            }
            .onSuccess {
                PassLogger.i(TAG, "Default vault created")
                syncStatusRepository.setMode(SyncMode.Background)
                syncStatusRepository.emit(ItemSyncStatus.SyncSuccess)
            }
    }

    private fun handleExistingShares(userId: UserId, refreshSharesResult: RefreshSharesResult) {
        PassLogger.i(TAG, "Received a list of shares, starting fetch items worker")
        val request = FetchItemsWorker.getRequestFor(
            source = FetchItemsWorker.FetchSource.ForceSync,
            userId = userId,
            shareIds = refreshSharesResult.allShareIds.toList()
        )
        workManager.enqueueUniqueWork(
            FetchItemsWorker.getOneTimeUniqueWorkName(userId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private companion object {

        private const val TAG = "RefreshContentImpl"

    }

}
