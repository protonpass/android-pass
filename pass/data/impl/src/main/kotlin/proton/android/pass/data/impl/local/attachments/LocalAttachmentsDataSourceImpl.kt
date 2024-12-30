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

package proton.android.pass.data.impl.local.attachments

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.attachments.AttachmentEntity
import proton.android.pass.data.impl.db.entities.attachments.AttachmentWithChunks
import proton.android.pass.data.impl.db.entities.attachments.ChunkEntity
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import javax.inject.Inject

class LocalAttachmentsDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalAttachmentsDataSource {

    override suspend fun saveAttachmentsWithChunks(
        attachmentEntities: List<AttachmentEntity>,
        chunkEntities: List<ChunkEntity>
    ) {
        database.inTransaction {
            database.attachmentDao().insertOrUpdate(*attachmentEntities.toTypedArray())
            database.chunkDao().insertOrUpdate(*chunkEntities.toTypedArray())
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
        shareId: ShareId,
        itemId: ItemId,
        attachmentIdList: List<AttachmentId>
    ) {
        database.attachmentDao().removeByAttachments(
            shareId = shareId.id,
            itemId = itemId.id,
            attachmentIds = attachmentIdList.map { it.id }
        )
    }

    override fun observeActiveAttachmentsWithChunksForItem(
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<AttachmentWithChunks>> = database.itemsDao().observeById(shareId.id, itemId.id)
        .flatMapLatest { item ->
            observeAttachments(
                shareId = shareId,
                itemId = itemId,
                revision = item.revision
            )
        }

    override fun observeAllAttachmentsWithChunksForItemRevisions(
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<AttachmentWithChunks>> = observeAttachments(
        shareId = shareId,
        itemId = itemId,
        revision = null
    )

    private fun observeAttachments(
        shareId: ShareId,
        itemId: ItemId,
        revision: Long?
    ) = combine(
        database.attachmentDao().observeItemAttachments(
            shareId = shareId.id,
            itemId = itemId.id,
            revision = revision
        ),
        database.chunkDao().observeItemChunks(
            shareId = shareId.id,
            itemId = itemId.id
        )
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
