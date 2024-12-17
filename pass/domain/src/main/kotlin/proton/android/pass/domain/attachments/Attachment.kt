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
value class AttachmentId(val id: String)

enum class AttachmentType(val id: Int) {
    RasterImage(1),
    VectorImage(2),
    Photo(3),
    Video(4),
    Audio(5),
    Key(6),
    Text(7),
    Calendar(8),
    Pdf(9),
    Word(10),
    PowerPoint(11),
    Excel(12),
    Document(13),
    Unknown(0)
}

data class Attachment(
    val id: AttachmentId,
    val shareId: ShareId,
    val itemId: ItemId,
    val name: String,
    val mimeType: String,
    val type: AttachmentType,
    val size: Long,
    val createTime: Instant,
    val reencryptedKey: EncryptedByteArray,
    val chunks: List<Chunk>
)

@JvmInline
value class ChunkId(val id: String)

data class Chunk(
    val id: ChunkId,
    val size: Long,
    val index: Int
)
