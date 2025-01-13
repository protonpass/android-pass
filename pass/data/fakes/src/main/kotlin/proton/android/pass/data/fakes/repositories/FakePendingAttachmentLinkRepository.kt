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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.PendingAttachmentId
import javax.inject.Inject

class FakePendingAttachmentLinkRepository @Inject constructor() : PendingAttachmentLinkRepository {

    private val toLinkMap = mutableMapOf<PendingAttachmentId, EncryptionKey>()
    private val toUnlinkSet = mutableSetOf<AttachmentId>()

    override fun addToLink(attachmentId: PendingAttachmentId, encryptionKey: EncryptionKey) {
        toLinkMap[attachmentId] = encryptionKey
    }

    override fun addToUnLink(attachmentId: AttachmentId) {
        toUnlinkSet.add(attachmentId)
    }

    override fun addAllToUnLink(list: Set<AttachmentId>) {
        toUnlinkSet.addAll(list)
    }

    override fun getToLinkKey(attachmentId: PendingAttachmentId): EncryptionKey? = toLinkMap[attachmentId]

    override fun getAllToLink(): Map<PendingAttachmentId, EncryptionKey> = toLinkMap.toMap()

    override fun observeAllToLink(): StateFlow<Map<PendingAttachmentId, EncryptionKey>> =
        MutableStateFlow(toLinkMap.toMap())

    override fun getAllToUnLink(): Set<AttachmentId> = toUnlinkSet.toSet()

    override fun observeAllToUnLink(): Flow<Set<AttachmentId>> = flow {
        emit(toUnlinkSet.toSet())
    }

    override fun removeToLink(attachmentId: PendingAttachmentId) {
        toLinkMap.remove(attachmentId)
    }

    override fun clearAll() {
        toLinkMap.clear()
        toUnlinkSet.clear()
    }
}
