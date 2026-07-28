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

package proton.android.pass.data.impl.usecases.attachments

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.usecases.attachments.AttachmentDownloadProgress
import proton.android.pass.data.api.usecases.attachments.ObserveAttachmentDownloadProgress
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveAttachmentDownloadProgressImpl @Inject constructor(
    private val attachmentRepository: AttachmentRepository
) : ObserveAttachmentDownloadProgress {

    override fun invoke(userId: UserId, shareIds: List<ShareId>): Flow<AttachmentDownloadProgress> = combine(
        attachmentRepository.observeDownloadProgress(userId, shareIds),
        attachmentRepository.observePendingDownloads(userId, shareIds, includeFailed = true)
    ) { (downloaded, total), pendingDownloads ->
        AttachmentDownloadProgress(
            downloaded = downloaded,
            total = total,
            failedAttachments = pendingDownloads
                .filter { it.downloadStatus == AttachmentDownloadStatus.Failed }
        )
    }
}
