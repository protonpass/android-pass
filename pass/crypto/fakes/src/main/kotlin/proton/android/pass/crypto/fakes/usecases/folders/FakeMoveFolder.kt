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
import proton.android.pass.crypto.api.usecases.folders.MoveFolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeMoveFolder @Inject constructor() : MoveFolder {

    private val memory = mutableListOf<Payload>()
    private var mockResult: String? = null

    fun memory(): List<Payload> = memory

    fun setResult(reencryptedFolderKey: String) {
        mockResult = reencryptedFolderKey
    }

    override fun reencryptFolderKey(folderKey: EncryptionKey, newParentKey: EncryptionKey): String {
        memory.add(Payload(folderKey, newParentKey))

        return mockResult ?: Base64.encodeBase64String("reencrypted-folder-key".toByteArray())
    }

    data class Payload(
        val folderKey: EncryptionKey,
        val newParentKey: EncryptionKey
    )
}
