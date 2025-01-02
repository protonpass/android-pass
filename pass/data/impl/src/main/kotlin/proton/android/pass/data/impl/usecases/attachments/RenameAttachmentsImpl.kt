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

package proton.android.pass.data.impl.usecases.attachments

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.repositories.PendingAttachmentUpdaterRepository
import proton.android.pass.data.api.usecases.attachments.RenameAttachments
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RenameAttachmentsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val pendingAttachmentUpdaterRepository: PendingAttachmentUpdaterRepository,
    private val attachmentRepository: AttachmentRepository
) : RenameAttachments {

    override suspend fun invoke(shareId: ShareId, itemId: ItemId) {
        val pendingRenames: Map<AttachmentId, String> =
            pendingAttachmentUpdaterRepository.getAllPendingRenames()
        if (pendingRenames.isEmpty()) return
        val userId = accountManager.getPrimaryUserId().firstOrNull()
            ?: throw UserIdNotAvailableError()
        runConcurrently(
            items = pendingRenames.entries,
            block = { (attachmentId, rename) ->
                attachmentRepository.updateFileMetadata(
                    userId = userId,
                    shareId = shareId,
                    itemId = itemId,
                    attachmentId = attachmentId,
                    title = rename
                )
            },
            onSuccess = { _, _ ->
                PassLogger.i(TAG, "Successfully renamed attachments")
            },
            onFailure = { _, error ->
                PassLogger.w(TAG, "Failed to rename attachments")
                PassLogger.w(TAG, error)
                throw error
            }
        )
    }

    companion object {
        private const val TAG = "RenameAttachmentsImpl"
    }
}
