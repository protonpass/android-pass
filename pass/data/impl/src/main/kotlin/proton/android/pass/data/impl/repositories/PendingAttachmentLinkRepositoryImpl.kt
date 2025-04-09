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

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.data.api.repositories.PendingAttachmentLinkData
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.PendingAttachmentId
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingAttachmentLinkRepositoryImpl @Inject constructor() : PendingAttachmentLinkRepository {

    private val toLink = MutableStateFlow<Map<PendingAttachmentId, PendingAttachmentLinkData>>(emptyMap())
    private val toUnlink = MutableStateFlow<Set<AttachmentId>>(emptySet())

    override fun addToLink(attachmentId: PendingAttachmentId, linkData: PendingAttachmentLinkData) {
        toLink.update { currentMap ->
            ConcurrentHashMap(currentMap).apply {
                this[attachmentId] = linkData
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

    override fun getToLinkData(attachmentId: PendingAttachmentId): PendingAttachmentLinkData? =
        toLink.value[attachmentId]?.let { data ->
            PendingAttachmentLinkData(
                linkKey = data.linkKey.clone(),
                encryptionVersion = data.encryptionVersion,
                numChunks = data.numChunks
            )
        }

    override fun getAllToLink(): Map<PendingAttachmentId, PendingAttachmentLinkData> = toLink.value

    override fun observeAllToLink(): StateFlow<Map<PendingAttachmentId, PendingAttachmentLinkData>> = toLink

    override fun getAllToUnLink(): Set<AttachmentId> = toUnlink.value

    override fun observeAllToUnLink(): StateFlow<Set<AttachmentId>> = toUnlink

    override fun removeToLink(attachmentId: PendingAttachmentId) {
        toLink.update { currentMap ->
            ConcurrentHashMap(currentMap).apply {
                remove(attachmentId)
            }.toMap()
        }
    }

    override fun clearAll() {
        toLink.update { emptyMap() }
        toUnlink.update { emptySet() }
    }
}
