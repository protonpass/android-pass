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
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.usecases.attachments.UploadAttachment
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UploadAttachmentImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val attachmentRepository: AttachmentRepository,
    private val draftAttachmentRepository: DraftAttachmentRepository
) : UploadAttachment {

    override suspend fun invoke(uri: URI) {
        runCatching {
            val userId = accountManager.getPrimaryUserId().firstOrNull()
                ?: throw UserIdNotAvailableError()
            val attachmentId = attachmentRepository.createPendingAttachment(
                userId = userId,
                uri = uri
            )
            attachmentRepository.uploadPendingAttachment(
                userId = userId,
                attachmentId = attachmentId,
                uri = uri
            )
        }
            .onFailure {
                draftAttachmentRepository.remove(uri)
                throw it
            }
    }
}
