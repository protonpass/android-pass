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

    override fun create(
        parentKey: EncryptionKey,
        keyRotation: Long,
        folderName: String
    ): CreateFolderPayload {
        memory.add(Payload(parentKey.value().copyOf(), keyRotation, folderName))

        val result = mockResult ?: run {
            // Default implementation if no mock result set
            val folderKey = EncryptionKey.generate()
            EncryptedCreateFolder(
                keyRotation = keyRotation,
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
        val parentKeyValue: ByteArray,
        val keyRotation: Long,
        val folderName: String
    ) {
        @Suppress("ReturnCount")
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Payload

            if (!parentKeyValue.contentEquals(other.parentKeyValue)) return false
            if (keyRotation != other.keyRotation) return false
            if (folderName != other.folderName) return false

            return true
        }

        override fun hashCode(): Int {
            var result = parentKeyValue.contentHashCode()
            result = 31 * result + keyRotation.hashCode()
            result = 31 * result + folderName.hashCode()
            return result
        }
    }
}
