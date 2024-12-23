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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.domain.attachments.AttachmentId
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

interface PendingAttachmentLinkRepository {
    fun addToLink(attachmentId: AttachmentId, encryptionKey: EncryptionKey)
    fun addToUnLink(attachmentId: AttachmentId)
    fun addAllToUnLink(list: Set<AttachmentId>)
    fun getToLinkKey(attachmentId: AttachmentId): EncryptionKey?
    fun getAllToLink(): Map<AttachmentId, EncryptionKey>
    fun observeAllToLink(): Flow<Map<AttachmentId, EncryptionKey>>
    fun getAllToUnLink(): Set<AttachmentId>
    fun observeAllToUnLink(): Flow<Set<AttachmentId>>
    fun removeToLink(attachmentId: AttachmentId): Boolean
    fun removeToUnlink(attachmentId: AttachmentId): Boolean
    fun clearAll()
}

@Singleton
class PendingAttachmentLinkRepositoryImpl @Inject constructor() : PendingAttachmentLinkRepository {

    private val toLink = MutableStateFlow<Map<AttachmentId, EncryptionKey>>(emptyMap())
    private val toUnlink = MutableStateFlow<Set<AttachmentId>>(emptySet())

    override fun addToLink(attachmentId: AttachmentId, encryptionKey: EncryptionKey) {
        toLink.update { currentMap ->
            ConcurrentHashMap(currentMap).apply {
                this[attachmentId] = encryptionKey
            }.toMap()
        }
    }

    override fun addToUnLink(attachmentId: AttachmentId) {
        toUnlink.update { currentSet ->
            currentSet.toMutableSet().apply {
                add(attachmentId)
            }
        }
    }

    override fun addAllToUnLink(list: Set<AttachmentId>) {
        toUnlink.update { currentSet ->
            currentSet.toMutableSet().apply {
                addAll(list)
            }
        }
    }

    override fun getToLinkKey(attachmentId: AttachmentId): EncryptionKey? = toLink.value[attachmentId]?.clone()

    override fun getAllToLink(): Map<AttachmentId, EncryptionKey> = toLink.value

    override fun observeAllToLink(): StateFlow<Map<AttachmentId, EncryptionKey>> = toLink

    override fun getAllToUnLink(): Set<AttachmentId> = toUnlink.value

    override fun observeAllToUnLink(): StateFlow<Set<AttachmentId>> = toUnlink

    override fun removeToLink(attachmentId: AttachmentId): Boolean {
        var removed = false
        toLink.update { currentMap ->
            ConcurrentHashMap(currentMap).apply {
                removed = this.remove(attachmentId) != null
            }.toMap()
        }
        return removed
    }

    override fun removeToUnlink(attachmentId: AttachmentId): Boolean {
        var removed = false
        toUnlink.update { currentSet ->
            currentSet.toMutableSet().apply {
                removed = this.remove(attachmentId)
            }
        }
        return removed
    }

    override fun clearAll() {
        toLink.update { emptyMap() }
        toUnlink.update { emptySet() }
    }
}
