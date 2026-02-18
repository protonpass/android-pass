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

package proton.android.pass.crypto.impl.usecases.folders

import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.folders.CreateFolder
import proton.android.pass.crypto.api.usecases.folders.CreateFolderPayload
import proton.android.pass.crypto.api.usecases.folders.EncryptedCreateFolder
import proton.android.pass.crypto.impl.Constants.FOLDER_CONTENT_FORMAT_VERSION
import proton_pass_folder_v1.FolderV1
import javax.inject.Inject

class CreateFolderImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : CreateFolder {

    override fun create(
        parentKey: EncryptionKey,
        keyRotation: Long,
        folderName: String
    ): CreateFolderPayload {
        val folder = FolderV1.Folder.newBuilder()
            .setName(folderName)
            .build()
        val serializedFolder = folder.toByteArray()

        val folderKey = EncryptionKey.generate()

        val encryptedContents = encryptionContextProvider.withEncryptionContext(folderKey.clone()) {
            encrypt(serializedFolder, EncryptionTag.FolderContent)
        }

        val encryptedFolderKey = encryptionContextProvider.withEncryptionContext(parentKey.clone()) {
            encrypt(folderKey.value(), EncryptionTag.FolderKey)
        }

        val request = EncryptedCreateFolder(
            keyRotation = keyRotation,
            contentFormatVersion = FOLDER_CONTENT_FORMAT_VERSION,
            content = Base64.encodeBase64String(encryptedContents.array),
            folderKey = Base64.encodeBase64String(encryptedFolderKey.array)
        )

        return CreateFolderPayload(
            request = request,
            folderKey = folderKey
        )
    }
}
