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
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.repositories.SyncMode
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.RefreshSharesAndEnqueueSync
import proton.android.pass.data.api.usecases.RefreshSharesResult
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.impl.R
import proton.android.pass.data.impl.work.FetchItemsWorker
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.entity.NewVault
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.InternalSettingsRepository
import javax.inject.Inject
import proton.android.pass.data.api.repositories.RefreshSharesResult as RepositoryRefreshSharesResult

class RefreshSharesAndEnqueueSyncImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val shareRepository: ShareRepository,
    private val itemSyncStatusRepository: ItemSyncStatusRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val createVault: CreateVault,
    private val canCreateVault: CanCreateVault,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val preferencesRepository: FeatureFlagsPreferencesRepository,
    private val workManager: WorkManager
) : RefreshSharesAndEnqueueSync {

    override suspend fun invoke(userId: UserId, syncType: RefreshSharesAndEnqueueSync.SyncType): RefreshSharesResult {
        PassLogger.i(
            TAG,
            "RefreshSharesAndEnqueueSync started for user: $userId with syncType: $syncType"
        )

        if (syncType == RefreshSharesAndEnqueueSync.SyncType.FULL) {
            PassLogger.i(TAG, "FULL sync requested, setting up sync status")
            itemSyncStatusRepository.clear()
            itemSyncStatusRepository.setMode(SyncMode.ShownToUser)
            itemSyncStatusRepository.emit(ItemSyncStatus.SyncStarted)
        }

        return safeRunCatching {
            val repositoryResult = shareRepository.refreshShares(userId)
            PassLogger.i(TAG, "Shares for user: $userId refreshed")

            if (repositoryResult.allShareIds.isEmpty()) {
                handleEmptyShares(userId)
            } else {
                handleNonEmptyShares(userId, repositoryResult, syncType)
            }
        }.onFailure {
            if (syncType == RefreshSharesAndEnqueueSync.SyncType.FULL) {
                PassLogger.w(TAG, "Error during FULL sync")
                PassLogger.w(TAG, it)
                itemSyncStatusRepository.emit(ItemSyncStatus.SyncError)
            }
        }.getOrThrow()
    }

    private fun handleNonEmptyShares(
        userId: UserId,
        repositoryResult: RepositoryRefreshSharesResult,
        syncType: RefreshSharesAndEnqueueSync.SyncType
    ): RefreshSharesResult.SharesFound {
        val existingShareIds = repositoryResult.allShareIds - repositoryResult.newShareIds
        val (sharesToFetch, fetchSource) = getSharesAndSource(repositoryResult, syncType)

        val shouldEnqueueWorker = sharesToFetch.isNotEmpty()
        if (shouldEnqueueWorker) {
            enqueueWorker(userId, sharesToFetch, fetchSource)
        }

        return RefreshSharesResult.SharesFound(
            shareIds = existingShareIds,
            isWorkerEnqueued = shouldEnqueueWorker
        )
    }

    private fun getSharesAndSource(
        result: RepositoryRefreshSharesResult,
        syncType: RefreshSharesAndEnqueueSync.SyncType
    ): Pair<Set<ShareId>, FetchItemsWorker.FetchSource> = when (syncType) {
        RefreshSharesAndEnqueueSync.SyncType.INCREMENTAL ->
            result.newShareIds to if (result.wasFirstSync) {
                FetchItemsWorker.FetchSource.FirstSync
            } else {
                FetchItemsWorker.FetchSource.NewShare
            }

        RefreshSharesAndEnqueueSync.SyncType.FULL ->
            result.allShareIds to FetchItemsWorker.FetchSource.ForceSync
    }

    private fun enqueueWorker(
        userId: UserId,
        shareIds: Set<ShareId>,
        fetchSource: FetchItemsWorker.FetchSource
    ) {
        val request = FetchItemsWorker.getRequestFor(
            source = fetchSource,
            userId = userId,
            shareIds = shareIds.toList()
        )
        PassLogger.i(TAG, "Enqueuing FetchItemsWorker with source: $fetchSource")
        workManager.enqueueUniqueWork(
            FetchItemsWorker.getOneTimeUniqueWorkName(userId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private suspend fun handleEmptyShares(userId: UserId): RefreshSharesResult {
        if (!canCreateVault().first()) {
            PassLogger.i(TAG, "Skipping default vault creation")
            setSyncSuccess()
            return RefreshSharesResult.NoSharesSkipped
        }

        val allowNoVault = preferencesRepository
            .get<Boolean>(FeatureFlag.PASS_ALLOW_NO_VAULT)
            .firstOrNull()
            ?: false
        val hasDefaultVaultBeenCreated = internalSettingsRepository
            .hasDefaultVaultBeenCreated(userId)
            .firstOrNull()
            ?: false

        return if (shouldCreateDefaultVault(allowNoVault, hasDefaultVaultBeenCreated)) {
            PassLogger.i(TAG, "Creating default vault")
            createDefaultVault(userId)
            internalSettingsRepository.setDefaultVaultHasBeenCreated(userId)
            setSyncSuccess()
            RefreshSharesResult.NoSharesVaultCreated
        } else {
            PassLogger.i(TAG, "Default vault already created")
            setSyncSuccess()
            RefreshSharesResult.NoSharesSkipped
        }
    }

    private fun shouldCreateDefaultVault(allowNoVault: Boolean, hasBeenCreated: Boolean): Boolean =
        !allowNoVault || !hasBeenCreated

    private suspend fun createDefaultVault(userId: UserId) {
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

    private suspend fun setSyncSuccess() {
        itemSyncStatusRepository.setMode(SyncMode.Background)
        itemSyncStatusRepository.emit(ItemSyncStatus.SyncSuccess)
    }

    private companion object {
        private const val TAG = "RefreshSharesAndEnqueueSyncImpl"
    }
}
