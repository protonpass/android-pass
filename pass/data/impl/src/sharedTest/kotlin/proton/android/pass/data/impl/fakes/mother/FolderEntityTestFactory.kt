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

package proton.android.pass.data.impl.fakes.mother

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.data.impl.db.entities.FolderEntity
import proton_pass_folder_v1.FolderV1

object FolderEntityTestFactory {

    fun create(
        id: String = "folder-id",
        userId: String = "user-id",
        shareId: String = "share-id",
        vaultId: String = "vault-id",
        parentFolderId: String? = null,
        keyRotation: Long = 1,
        contentFormatVersion: Int = 1,
        content: String = "",
        folderKey: String = "folder-key",
        name: String = "Folder",
        encryptedContent: EncryptedByteArray = encryptFolderName(name)
    ): FolderEntity = FolderEntity(
        id = id,
        userId = userId,
        shareId = shareId,
        vaultId = vaultId,
        parentFolderId = parentFolderId,
        keyRotation = keyRotation,
        contentFormatVersion = contentFormatVersion,
        content = content,
        folderKey = folderKey,
        encryptedContent = encryptedContent
    )

    private fun encryptFolderName(name: String): EncryptedByteArray {
        val proto = FolderV1.Folder.newBuilder().setName(name).build()
        return FakeEncryptionContext.encrypt(proto.toByteArray())
    }
}
