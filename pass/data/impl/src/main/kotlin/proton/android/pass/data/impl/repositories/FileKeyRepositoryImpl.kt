/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.repositories

import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.domain.attachments.AttachmentId
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

interface FileKeyRepository {
    fun addMapping(attachmentId: AttachmentId, encryptionKey: EncryptionKey)
    fun getEncryptionKey(attachmentId: AttachmentId): EncryptionKey?
    fun getAllMappings(): Map<AttachmentId, EncryptionKey>
    fun removeMapping(attachmentId: AttachmentId): Boolean
    fun clearAllMappings()
}

class FileKeyRepositoryImpl @Inject constructor() : FileKeyRepository {
    private val storage: MutableMap<AttachmentId, EncryptionKey> = ConcurrentHashMap()

    override fun addMapping(attachmentId: AttachmentId, encryptionKey: EncryptionKey) {
        storage[attachmentId] = encryptionKey
    }

    override fun getEncryptionKey(attachmentId: AttachmentId): EncryptionKey? = storage[attachmentId]?.clone()

    override fun getAllMappings(): Map<AttachmentId, EncryptionKey> = storage.toMap()

    override fun removeMapping(attachmentId: AttachmentId): Boolean = storage.remove(attachmentId) != null

    override fun clearAllMappings() {
        storage.clear()
    }
}
