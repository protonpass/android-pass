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

package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import proton.android.pass.data.impl.db.entities.attachments.AttachmentEntity
import proton.android.pass.data.impl.db.entities.attachments.AttachmentWithChunks
import proton.android.pass.data.impl.db.entities.attachments.ChunkEntity
import proton.android.pass.data.impl.local.attachments.LocalAttachmentsDataSource
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId

class FakeLocalAttachmentsDataSource : LocalAttachmentsDataSource {

    override suspend fun removeAttachmentsForItem(shareId: ShareId, itemId: ItemId) = Unit

    override suspend fun removeAttachmentsById(
        shareId: ShareId,
        itemId: ItemId,
        attachmentIdList: List<AttachmentId>
    ) = Unit

    override fun observeActiveAttachmentsWithChunksForItem(
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<AttachmentWithChunks>> = emptyFlow()

    override fun observeAllAttachmentsWithChunksForItemRevisions(
        shareId: ShareId,
        itemId: ItemId
    ): Flow<List<AttachmentWithChunks>> = emptyFlow()

    override suspend fun saveAttachmentsWithChunks(
        attachmentEntities: List<AttachmentEntity>,
        chunkEntities: List<ChunkEntity>
    ) = Unit

    override suspend fun getAttachmentById(
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): AttachmentEntity? = null

    override suspend fun getChunksForAttachment(
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): List<ChunkEntity> = emptyList()

    override suspend fun updateAttachment(attachmentEntity: AttachmentEntity) = Unit
}
