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
import proton.android.pass.crypto.api.usecases.folders.EncryptedUpdateFolder
import proton.android.pass.crypto.api.usecases.folders.UpdateFolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUpdateFolder @Inject constructor() : UpdateFolder {

    private val memory = mutableListOf<Payload>()
    private var mockResult: EncryptedUpdateFolder? = null

    fun memory(): List<Payload> = memory

    fun setResult(
        keyRotation: Long,
        contentFormatVersion: Int,
        content: String
    ) {
        mockResult = EncryptedUpdateFolder(
            keyRotation = keyRotation,
            contentFormatVersion = contentFormatVersion,
            content = content
        )
    }

    override fun update(
        folderKey: EncryptionKey,
        keyRotation: Long,
        folderName: String
    ): EncryptedUpdateFolder {
        memory.add(Payload(folderKey, keyRotation, folderName))

        return mockResult ?: EncryptedUpdateFolder(
            keyRotation = keyRotation,
            contentFormatVersion = 1,
            content = Base64.encodeBase64String(folderName.toByteArray())
        )
    }

    data class Payload(
        val folderKey: EncryptionKey,
        val keyRotation: Long,
        val folderName: String
    )
}
