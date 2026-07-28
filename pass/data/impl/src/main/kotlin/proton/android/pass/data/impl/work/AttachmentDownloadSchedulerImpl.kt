/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.work

import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.attachments.AttachmentDownloadScheduler
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttachmentDownloadSchedulerImpl @Inject constructor(
    private val workManager: WorkManager,
    private val shareRepository: ShareRepository,
    private val attachmentRepository: AttachmentRepository,
    private val preferencesRepository: UserPreferencesRepository
) : AttachmentDownloadScheduler {

    override suspend fun scheduleAll(userId: UserId, force: Boolean) {
        val allShareIds = shareRepository.observeOfflineEnabledShareIds(userId).first()
        if (allShareIds.isEmpty()) {
            PassLogger.i(TAG, "No offline-enabled shares, nothing to schedule")
            return
        }

        val itemShareIds = shareRepository.filterShareIdsByType(
            userId,
            allShareIds.toSet(),
            ShareType.Item
        )
        val vaultShareIds = allShareIds.filter { it !in itemShareIds }

        val wifiOnly = !preferencesRepository.observeAllowCellularDownloadPref(userId)
            .first()
            .value()

        // KEEP for periodic/automatic schedules to avoid interrupting in-flight downloads.
        // REPLACE for user-initiated schedules (constraint changed, retry) that must
        // re-evaluate constraints immediately.
        val policy = if (force) ExistingWorkPolicy.REPLACE else ExistingWorkPolicy.KEEP

        vaultShareIds.forEach { shareId ->
            enqueueWorker(userId, shareId, wifiOnly, policy, includeFailed = force)
        }

        // Schedule a single worker for all shared items
        val sharedItemsEnabled = preferencesRepository
            .observeSharedItemsDownloadPref(userId)
            .first()
            .value()
        if (sharedItemsEnabled && itemShareIds.isNotEmpty()) {
            enqueueSharedItemsWorker(userId, itemShareIds.toList(), wifiOnly, policy, includeFailed = force)
        }

        val totalWorkers = vaultShareIds.size + if (sharedItemsEnabled && itemShareIds.isNotEmpty()) 1 else 0
        PassLogger.i(TAG, "Scheduled $totalWorkers attachment download workers (force=$force)")
    }

    override suspend fun scheduleVault(userId: UserId, shareId: ShareId) {
        val wifiOnly = !preferencesRepository.observeAllowCellularDownloadPref(userId)
            .first()
            .value()

        enqueueWorker(userId, shareId, wifiOnly)
    }

    override suspend fun scheduleAttachment(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ) {
        val wifiOnly = !preferencesRepository.observeAllowCellularDownloadPref(userId)
            .first()
            .value()

        val itemShareIds = shareRepository.filterShareIdsByType(userId, setOf(shareId), ShareType.Item)
        val workName = if (shareId in itemShareIds) {
            AttachmentDownloadWorker.sharedItemsWorkName(userId)
        } else {
            AttachmentDownloadWorker.uniqueWorkName(shareId)
        }

        val request = AttachmentDownloadWorker.getRequestForAttachment(
            userId = userId,
            shareId = shareId,
            itemId = itemId,
            attachmentId = attachmentId,
            wifiOnly = wifiOnly
        )
        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }

    override suspend fun cancelVault(userId: UserId, shareId: ShareId) {
        workManager.cancelUniqueWork(AttachmentDownloadWorker.uniqueWorkName(shareId))
        attachmentRepository.clearDownloadedAttachments(userId, listOf(shareId))
        PassLogger.i(TAG, "Cancelled and cleaned attachment download")
    }

    override suspend fun cancelSharedItems(userId: UserId) {
        workManager.cancelUniqueWork(AttachmentDownloadWorker.sharedItemsWorkName(userId))
        val allShareIds = shareRepository.observeOfflineEnabledShareIds(userId).first()
        val itemShareIds = shareRepository.filterShareIdsByType(
            userId,
            allShareIds.toSet(),
            ShareType.Item
        )
        attachmentRepository.clearDownloadedAttachments(userId, itemShareIds.toList())
        PassLogger.i(TAG, "Cancelled and cleaned shared items download workers")
    }

    override suspend fun cancelAll(userId: UserId) {
        workManager.cancelAllWorkByTag(AttachmentDownloadWorker.userTag(userId))
        attachmentRepository.clearAllDownloadedAttachments(userId)
        PassLogger.i(TAG, "Cancelled and cleaned all attachment download workers")
    }

    private fun enqueueWorker(
        userId: UserId,
        shareId: ShareId,
        wifiOnly: Boolean,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE,
        includeFailed: Boolean = false
    ) {
        val request = AttachmentDownloadWorker.getRequestFor(userId, listOf(shareId), wifiOnly, includeFailed)
        workManager.enqueueUniqueWork(
            AttachmentDownloadWorker.uniqueWorkName(shareId),
            policy,
            request
        )
    }

    private fun enqueueSharedItemsWorker(
        userId: UserId,
        shareIds: List<ShareId>,
        wifiOnly: Boolean,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE,
        includeFailed: Boolean = false
    ) {
        val request = AttachmentDownloadWorker.getRequestFor(userId, shareIds, wifiOnly, includeFailed)
        workManager.enqueueUniqueWork(
            AttachmentDownloadWorker.sharedItemsWorkName(userId),
            policy,
            request
        )
    }

    private companion object {
        private const val TAG = "AttachmentDownloadSchedulerImpl"
    }
}
