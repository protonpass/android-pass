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

package proton.android.pass.crypto.fakes.usecases.folders

import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.usecases.folders.CreateFolder
import proton.android.pass.crypto.api.usecases.folders.CreateFolderPayload
import proton.android.pass.crypto.api.usecases.folders.EncryptedCreateFolder
import proton.android.pass.domain.key.ShareKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeCreateFolder @Inject constructor() : CreateFolder {

    private val memory = mutableListOf<Payload>()
    private var mockResult: EncryptedCreateFolder? = null

    fun memory(): List<Payload> = memory

    fun setResult(
        keyRotation: Long,
        contentFormatVersion: Int,
        content: String,
        folderKey: String
    ) {
        mockResult = EncryptedCreateFolder(
            keyRotation = keyRotation,
            contentFormatVersion = contentFormatVersion,
            content = content,
            folderKey = folderKey
        )
    }

    override fun create(shareKey: ShareKey, folderName: String): CreateFolderPayload {
        memory.add(Payload(shareKey, folderName))

        val result = mockResult ?: run {
            // Default implementation if no mock result set
            val folderKey = EncryptionKey.generate()
            EncryptedCreateFolder(
                keyRotation = shareKey.rotation,
                contentFormatVersion = 1,
                content = Base64.encodeBase64String(folderName.toByteArray()),
                folderKey = Base64.encodeBase64String(folderKey.value())
            )
        }

        return CreateFolderPayload(
            request = result,
            folderKey = EncryptionKey.generate()
        )
    }

    data class Payload(
        val shareKey: ShareKey,
        val folderName: String
    )
}
