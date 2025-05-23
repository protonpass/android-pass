/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.domain.attachments

import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

@JvmInline
value class PendingAttachmentId(val id: String)

@JvmInline
value class PersistentAttachmentId(val id: String)

@JvmInline
value class AttachmentId(val id: String)

enum class AttachmentType {
    RasterImage,
    VectorImage,
    Photo,
    Video,
    Audio,
    Key,
    Text,
    Calendar,
    Pdf,
    Word,
    PowerPoint,
    Excel,
    Document,
    Unknown
}

data class Attachment(
    val id: AttachmentId,
    val persistentId: PersistentAttachmentId,
    val shareId: ShareId,
    val itemId: ItemId,
    val name: String,
    val mimeType: String,
    val type: AttachmentType,
    val size: Long,
    val createTime: Instant,
    val modifyTime: Instant,
    val revisionAdded: Int,
    val revisionRemoved: Int?,
    val reencryptedKey: EncryptedByteArray,
    val chunks: List<Chunk>,
    val encryptionVersion: Int
) {
    fun existsForRevision(revision: Long): Boolean =
        revision >= revisionAdded && (revisionRemoved == null || revision < revisionRemoved)
}

@JvmInline
value class ChunkId(val id: String)

data class Chunk(
    val id: ChunkId,
    val size: Long,
    val index: Int
)
