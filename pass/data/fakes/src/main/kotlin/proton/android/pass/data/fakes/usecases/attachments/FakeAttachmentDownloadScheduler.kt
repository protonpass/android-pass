/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.fakes.usecases.attachments

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.attachments.AttachmentDownloadScheduler
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAttachmentDownloadScheduler @Inject constructor() : AttachmentDownloadScheduler {
    override suspend fun scheduleAll(userId: UserId, force: Boolean) {
        // no-op
    }

    override suspend fun scheduleVault(userId: UserId, shareId: ShareId) {
        // no-op
    }

    override suspend fun scheduleAttachment(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ) {
        // no-op
    }

    override suspend fun cancelVault(userId: UserId, shareId: ShareId) {
        // no-op
    }

    override suspend fun cancelSharedItems(userId: UserId) {
        // no-op
    }

    override suspend fun cancelAll(userId: UserId) {
        // no-op
    }
}
