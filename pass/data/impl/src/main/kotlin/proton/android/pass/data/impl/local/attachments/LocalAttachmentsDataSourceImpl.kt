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

package proton.android.pass.data.impl.local.attachments

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.attachments.AttachmentEntity
import proton.android.pass.data.impl.db.entities.attachments.AttachmentWithChunks
import proton.android.pass.data.impl.db.entities.attachments.ChunkEntity
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ItemId
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.data.impl.util.countPerSqliteChunk
import proton.android.pass.data.impl.util.isCompleteEncryptedFile
import proton.android.pass.data.impl.util.flowPerSqliteChunk
import proton.android.pass.data.impl.util.sqliteChunks
import proton.android.pass.domain.ShareId
import android.content.Context
import proton.android.pass.files.api.FilesDirectories
import dagger.hilt.android.qualifiers.ApplicationContext
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.domain.attachments.AttachmentId
import java.io.File
import javax.inject.Inject

@Suppress("TooManyFunctions")
class LocalAttachmentsDataSourceImpl @Inject constructor(
    private val database: PassDatabase,
    @ApplicationContext private val context: Context,
    private val appDispatchers: AppDispatchers
) : LocalAttachmentsDataSource {

    override suspend fun saveAttachmentsWithChunks(
        attachmentEntities: List<AttachmentEntity>,
        chunkEntities: List<ChunkEntity>
    ) {
        // Collect new attachment IDs that already have files on disk (e.g. just uploaded)
        val newEntitiesWithFiles = mutableListOf<AttachmentEntity>()

        database.inTransaction {
            val dao = database.attachmentDao()

            val existingByKey = attachmentEntities.map { it.id }
                .sqliteChunks(otherBoundArgs = 0)
                .flatMap { chunk -> dao.getAttachmentsByIds(chunk) }
                .associateBy { Triple(it.id, it.shareId, it.itemId) }

            attachmentEntities.forEach { entity ->
                val existing = existingByKey[Triple(entity.id, entity.shareId, entity.itemId)]

                if (existing != null) {
                    // Preserve the existing downloadStatus
                    dao.insertOrUpdate(entity.copy(downloadStatus = existing.downloadStatus))
                } else {
                    // New attachment — insert it (downloadStatus starts as Idle from FileApiModel.toEntity)
                    dao.insertOrUpdate(entity)
                    newEntitiesWithFiles.add(entity)
                }
            }

            database.chunkDao().insertOrUpdate(*chunkEntities.toTypedArray())
        }

        // Check filesystem outside the transaction
        val chunkCountByAttachment = chunkEntities.groupingBy { it.attachmentId }.eachCount()
        val downloaded = withContext(appDispatchers.io) {
            newEntitiesWithFiles.filter { entity ->
                val file = File(
                    context.filesDir,
                    FilesDirectories.AttachmentsEnc.buildPath(
                        entity.userId, entity.shareId, entity.itemId, entity.persistentId
                    )
                )
                val expectedChunkCount = chunkCountByAttachment[entity.id] ?: 0
                expectedChunkCount > 0 && isCompleteEncryptedFile(file, expectedChunkCount)
            }
        }
        downloaded.groupBy { it.userId }.forEach { (userId, entities) ->
            entities.map { it.id }
                .sqliteChunks(otherBoundArgs = 2)
                .forEach { chunk ->
                    database.attachmentDao().updateDownloadStatusForIds(
                        userId = userId,
                        attachmentIds = chunk,
                        downloadStatus = AttachmentDownloadStatus.Downloaded
                    )
                }
        }
    }

    override suspend fun getAttachmentById(
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): AttachmentEntity? = database.attachmentDao().getAttachmentById(
        shareId = shareId.id,
        itemId = itemId.id,
        attachmentId = attachmentId.id
    )

    override suspend fun getChunksForAttachment(
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): List<ChunkEntity> = database.chunkDao().getChunksForAttachment(
        shareId = shareId.id,
        itemId = itemId.id,
        attachmentId = attachmentId.id
    )

    override suspend fun updateAttachment(attachmentEntity: AttachmentEntity) {
        database.attachmentDao().update(attachmentEntity)
    }

    override suspend fun removeAttachmentsForItem(shareId: ShareId, itemId: ItemId) {
        database.attachmentDao().removeByItem(
            shareId = shareId.id,
            itemId = itemId.id
        )
    }

    override suspend fun removeAttachmentsById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentIdList: List<AttachmentId>
    ): List<String> {
        val dao = database.attachmentDao()
        val attachmentIds = attachmentIdList.map { it.id }
        val persistentIds = dao.getPersistentIdsByAttachmentIds(
            userId = userId.id,
            shareId = shareId.id,
            itemId = itemId.id,
            attachmentIds = attachmentIds
        )
        dao.removeByAttachments(
            shareId = shareId.id,
            itemId = itemId.id,
            attachmentIds = attachmentIds
        )
        return persistentIds
    }

    override fun observeActiveAttachmentsWithChunksForItem(
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<AttachmentWithChunks>> = observeAttachmentsWithChunks(
        database.attachmentDao().observeActiveItemAttachments(
            shareId = shareId.id,
            itemId = itemId.id
        ),
        database.chunkDao().observeItemChunks(
            shareId = shareId.id,
            itemId = itemId.id
        )
    )

    override fun observeAllAttachmentsWithChunksForItemRevisions(
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<AttachmentWithChunks>> = observeAttachmentsWithChunks(
        database.attachmentDao().observeAllItemRevisionsAttachments(
            shareId = shareId.id,
            itemId = itemId.id
        ),
        database.chunkDao().observeItemChunks(
            shareId = shareId.id,
            itemId = itemId.id
        )
    )

    override fun observeAllActiveAttachments(
        userId: UserId,
        shareIds: List<ShareId>
    ): Flow<List<AttachmentWithChunks>> {
        val shareIdStrings = shareIds.map { it.id }
        return observeAttachmentsWithChunks(
            shareIdStrings.flowPerSqliteChunk(otherBoundArgs = 1) {
                database.attachmentDao().observeAllActiveAttachments(userId.id, it)
            },
            shareIdStrings.flowPerSqliteChunk(otherBoundArgs = 1) {
                database.chunkDao().observeChunksByShareIds(userId.id, it)
            }
        )
    }

    override fun observePendingDownloads(
        userId: UserId,
        shareIds: List<ShareId>,
        includeFailed: Boolean
    ): Flow<List<AttachmentWithChunks>> {
        val shareIdStrings = shareIds.map { it.id }
        return observeAttachmentsWithChunks(
            shareIdStrings.flowPerSqliteChunk(otherBoundArgs = 1) {
                database.attachmentDao().observePendingDownloads(userId.id, it, includeFailed)
            },
            shareIdStrings.flowPerSqliteChunk(otherBoundArgs = 1) {
                database.chunkDao().observeChunksByShareIds(userId.id, it)
            }
        )
    }

    override suspend fun updateDownloadStatus(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId,
        status: AttachmentDownloadStatus
    ) {
        database.attachmentDao().updateDownloadStatus(
            userId = userId.id,
            shareId = shareId.id,
            itemId = itemId.id,
            attachmentId = attachmentId.id,
            downloadStatus = status
        )
    }

    override suspend fun resetDownloadingToPending(userId: UserId, shareIds: List<ShareId>) {
        resetStatusForShares(
            userId = userId,
            shareIds = shareIds,
            currentStatus = AttachmentDownloadStatus.Downloading
        )
    }

    override suspend fun resetDownloadingToIdle(userId: UserId, shareIds: List<ShareId>) {
        resetStatusForShares(
            userId = userId,
            shareIds = shareIds,
            currentStatus = AttachmentDownloadStatus.Downloading,
            newStatus = AttachmentDownloadStatus.Idle
        )
    }

    override suspend fun resetDownloadedToIdle(userId: UserId, shareIds: List<ShareId>) {
        resetStatusForShares(
            userId = userId,
            shareIds = shareIds,
            currentStatus = AttachmentDownloadStatus.Downloaded,
            newStatus = AttachmentDownloadStatus.Idle
        )
    }

    override suspend fun resetAllDownloadingToIdle(userId: UserId) {
        database.attachmentDao().resetDownloadStatusForUser(
            userId = userId.id,
            currentStatus = AttachmentDownloadStatus.Downloading,
            newStatus = AttachmentDownloadStatus.Idle
        )
    }

    override suspend fun resetAllDownloadedToIdle(userId: UserId) {
        database.attachmentDao().resetDownloadStatusForUser(
            userId = userId.id,
            currentStatus = AttachmentDownloadStatus.Downloaded,
            newStatus = AttachmentDownloadStatus.Idle
        )
    }

    override suspend fun resetIdleToPending(userId: UserId, shareIds: List<ShareId>) {
        resetStatusForShares(
            userId = userId,
            shareIds = shareIds,
            currentStatus = AttachmentDownloadStatus.Idle
        )
    }

    override suspend fun resetPausedToPending(userId: UserId, shareIds: List<ShareId>) {
        resetStatusForShares(
            userId = userId,
            shareIds = shareIds,
            currentStatus = AttachmentDownloadStatus.Paused
        )
    }

    override fun observeDownloadedCount(userId: UserId, shareIds: List<ShareId>): Flow<Int> =
        shareIds.map { it.id }.countPerSqliteChunk(otherBoundArgs = 1) {
            database.attachmentDao().observeDownloadedCount(userId.id, it)
        }

    override fun observeTotalCount(userId: UserId, shareIds: List<ShareId>): Flow<Int> =
        shareIds.map { it.id }.countPerSqliteChunk(otherBoundArgs = 1) {
            database.attachmentDao().observeTotalCount(userId.id, it)
        }

    override fun observeAttachmentById(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): Flow<AttachmentWithChunks?> = combine(
        database.attachmentDao().observeAttachmentById(
            userId = userId.id,
            shareId = shareId.id,
            itemId = itemId.id,
            attachmentId = attachmentId.id
        ),
        database.chunkDao().observeChunksForAttachment(
            userId = userId.id,
            shareId = shareId.id,
            itemId = itemId.id,
            attachmentId = attachmentId.id
        )
    ) { attachment, chunks ->
        attachment?.let {
            AttachmentWithChunks(
                attachment = it,
                chunks = chunks
            )
        }
    }

    private suspend fun resetStatusForShares(
        userId: UserId,
        shareIds: List<ShareId>,
        currentStatus: AttachmentDownloadStatus,
        newStatus: AttachmentDownloadStatus = AttachmentDownloadStatus.Pending
    ) {
        shareIds.map { it.id }
            .sqliteChunks(otherBoundArgs = 3)
            .forEach { chunk ->
                database.attachmentDao().resetDownloadStatusForShares(
                    userId = userId.id,
                    currentStatus = currentStatus,
                    newStatus = newStatus,
                    shareIds = chunk
                )
            }
    }

    private fun observeAttachmentsWithChunks(
        attachmentFlow: Flow<List<AttachmentEntity>>,
        chunkFlow: Flow<List<ChunkEntity>>
    ): Flow<List<AttachmentWithChunks>> = combine(
        attachmentFlow,
        chunkFlow
    ) { attachments, chunks ->
        val chunkMap = chunks.groupBy { it.attachmentId }

        attachments.map { attachment ->
            AttachmentWithChunks(
                attachment = attachment,
                chunks = chunkMap[attachment.id] ?: emptyList()
            )
        }
    }

}
