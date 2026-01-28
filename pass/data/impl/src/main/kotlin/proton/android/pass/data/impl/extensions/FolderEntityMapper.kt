/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.data.impl.extensions

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.data.impl.db.entities.FolderEntity
import proton.android.pass.data.impl.responses.FolderApiModel
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultId
import proton_pass_folder_v1.FolderV1

fun FolderApiModel.toEntity(
    userId: UserId,
    shareId: String,
    encryptedContent: EncryptedByteArray
): FolderEntity = FolderEntity(
    id = folderId,
    userId = userId.id,
    shareId = shareId,
    vaultId = vaultId,
    parentFolderId = parentFolderId,
    keyRotation = keyRotation,
    contentFormatVersion = contentFormatVersion,
    content = content,
    folderKey = folderKey,
    encryptedContent = encryptedContent
)

fun FolderEntity.toDomain(encryptionContext: EncryptionContext): Folder {
    val decrypted = encryptionContext.decrypt(encryptedContent)
    val parsed = FolderV1.Folder.parseFrom(decrypted)
    return Folder(
        userId = UserId(userId),
        shareId = ShareId(shareId),
        vaultId = VaultId(vaultId),
        folderId = FolderId(id),
        parentFolderId = parentFolderId?.let(::FolderId),
        folderKey = folderKey,
        name = parsed.name
    )
}
