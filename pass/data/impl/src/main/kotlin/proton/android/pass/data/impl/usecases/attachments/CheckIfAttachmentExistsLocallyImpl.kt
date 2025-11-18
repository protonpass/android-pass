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

package proton.android.pass.data.impl.usecases.attachments

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.data.api.repositories.AttachmentRepository
import proton.android.pass.data.api.usecases.attachments.CheckIfAttachmentExistsLocally
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.files.api.FilesDirectories
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CheckIfAttachmentExistsLocallyImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val accountManager: AccountManager,
    private val attachmentRepository: AttachmentRepository,
    private val appDispatchers: AppDispatchers
) : CheckIfAttachmentExistsLocally {

    override suspend fun invoke(
        shareId: ShareId,
        itemId: ItemId,
        attachmentId: AttachmentId
    ): Boolean {
        val userId = accountManager.getPrimaryUserId().firstOrNull() ?: return false
        val attachment =
            attachmentRepository.getAttachmentById(userId, shareId, itemId, attachmentId)
        return withContext(appDispatchers.io) {
            File(
                context.filesDir,
                buildString {
                    append(FilesDirectories.Attachments.value)
                    append("/")
                    append(userId.id)
                    append("/")
                    append(shareId.id)
                    append("/")
                    append(itemId.id)
                    append("/")
                    append(attachment.persistentId.id)
                }
            ).exists()
        }
    }
}
