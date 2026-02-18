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

package proton.android.pass.crypto.fakes.usecases.folders

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.usecases.folders.EncryptedFolderData
import proton.android.pass.crypto.api.usecases.folders.OpenFolder
import proton.android.pass.crypto.api.usecases.folders.OpenFolderOutput

class FakeOpenFolder : OpenFolder {
    data class OpenCall(
        val folderId: String,
        val parentKey: ByteArray
    )

    val calls = mutableListOf<OpenCall>()
    val keyReferences = mutableListOf<EncryptionKey>()
    private val outputByFolderId = mutableMapOf<String, OpenFolderOutput>()

    fun setOutput(
        folderId: String,
        folderName: String,
        reencryptedFolderKey: EncryptedByteArray
    ) {
        outputByFolderId[folderId] = OpenFolderOutput(
            folderName = folderName,
            reencryptedFolderKey = reencryptedFolderKey
        )
    }

    override suspend fun open(encryptedFolder: EncryptedFolderData, parentKey: EncryptionKey): OpenFolderOutput {
        keyReferences.add(parentKey)
        calls.add(
            OpenCall(
                folderId = encryptedFolder.folderId.id,
                parentKey = parentKey.value().copyOf()
            )
        )
        return outputByFolderId[encryptedFolder.folderId.id]
            ?: error("No OpenFolder output configured for ${encryptedFolder.folderId.id}")
    }
}
