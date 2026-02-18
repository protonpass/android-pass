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
import proton.android.pass.crypto.api.usecases.folders.MoveFolder
import javax.inject.Inject

class MoveFolderImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : MoveFolder {

    override fun reencryptFolderKey(folderKey: EncryptionKey, newParentKey: EncryptionKey): String {
        // Re-encrypt the folder key with the new parent key
        val encryptedFolderKey = encryptionContextProvider.withEncryptionContext(newParentKey) {
            encrypt(folderKey.value(), EncryptionTag.FolderKey)
        }

        return Base64.encodeBase64String(encryptedFolderKey.array)
    }
}
