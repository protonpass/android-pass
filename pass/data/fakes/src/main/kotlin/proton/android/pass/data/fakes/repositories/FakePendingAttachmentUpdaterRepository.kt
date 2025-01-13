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
import kotlinx.coroutines.flow.flow
import proton.android.pass.data.api.repositories.PendingAttachmentUpdaterRepository
import proton.android.pass.domain.attachments.AttachmentId
import javax.inject.Inject

class FakePendingAttachmentUpdaterRepository @Inject constructor() : PendingAttachmentUpdaterRepository {

    private val pendingRenames = mutableMapOf<AttachmentId, String>()

    override fun putPendingRename(attachmentId: AttachmentId, newName: String) {
        pendingRenames[attachmentId] = newName
    }

    override fun getPendingRename(attachmentId: AttachmentId): String? = pendingRenames[attachmentId]

    override fun getAllPendingRenames(): Map<AttachmentId, String> = pendingRenames.toMap()

    override fun observeAllPendingRenames(): Flow<Map<AttachmentId, String>> = flow {
        emit(pendingRenames.toMap())
    }

    override fun clearAll() {
        pendingRenames.clear()
    }
}
