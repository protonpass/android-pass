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

package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedFolderData
import proton.android.pass.crypto.api.usecases.OpenFolder
import proton.android.pass.crypto.api.usecases.OpenFolderOutput
import proton.android.pass.log.api.PassLogger
import proton_pass_folder_v1.FolderV1
import javax.inject.Inject

class OpenFolderImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : OpenFolder {

    override suspend fun open(encryptedFolder: EncryptedFolderData, parentKey: EncryptionKey): OpenFolderOutput =
        encryptionContextProvider.withEncryptionContextSuspendable {
            val decodedFolderKey = Base64.decodeBase64(encryptedFolder.folderKey)
            val decryptedFolderKey = encryptionContextProvider.withEncryptionContextSuspendable(parentKey) {
                EncryptionKey(decrypt(EncryptedByteArray(decodedFolderKey), EncryptionTag.FolderKey))
            }
            val folderKeyBytes = decryptedFolderKey.value()
            val decodedContent = Base64.decodeBase64(encryptedFolder.content)
            val decryptedContent = encryptionContextProvider.withEncryptionContextSuspendable(
                EncryptionKey(folderKeyBytes.clone())
            ) {
                decrypt(EncryptedByteArray(decodedContent), EncryptionTag.FolderContent)
            }
            val encryptedFolderKeyForStorage = encrypt(folderKeyBytes)
            if (encryptedFolder.contentFormatVersion > FOLDER_CONTENT_FORMAT_VERSION) {
                PassLogger.w(
                    TAG,
                    "Unknown Folder ContentFormatVersion:" +
                        " ${encryptedFolder.contentFormatVersion}"
                )
            }
            val parsed = FolderV1.Folder.parseFrom(decryptedContent)
            OpenFolderOutput(
                folderName = parsed.name,
                reencryptedFolderKey = encryptedFolderKeyForStorage
            )
        }

    private companion object {
        private const val TAG = "OpenFolderImpl"
        private const val FOLDER_CONTENT_FORMAT_VERSION = 1
    }
}
