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
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.attachments.RestoreAttachments
import proton.android.pass.data.impl.util.runConcurrently
import proton.android.pass.domain.ItemFlag
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreAttachmentsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val attachmentRepository: AttachmentRepository,
    private val itemRepository: ItemRepository
) : RestoreAttachments {
    override suspend fun invoke(
        shareId: ShareId,
        itemId: ItemId,
        toRestore: Set<AttachmentId>
    ) {
        if (toRestore.isEmpty()) return
        val userId = accountManager.getPrimaryUserId().firstOrNull()
            ?: throw UserIdNotAvailableError()
        runConcurrently(
            items = toRestore,
            block = { attachmentId ->
                attachmentRepository.restoreOldFile(userId, shareId, itemId, attachmentId)
            },
            onFailure = { _, error ->
                throw error
            }
        )
        itemRepository.updateLocalItemFlags(
            userId = userId,
            shareId = shareId,
            itemId = itemId,
            flag = ItemFlag.HasAttachments,
            isFlagEnabled = true
        )
    }
}
