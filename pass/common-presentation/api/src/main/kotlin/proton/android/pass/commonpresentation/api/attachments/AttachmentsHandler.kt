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

package proton.android.pass.commonpresentation.api.attachments

import android.content.Context
import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.Option
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.DraftAttachment
import proton.android.pass.domain.attachments.FileMetadata
import java.net.URI

interface AttachmentsHandler {

    val attachmentState: Flow<AttachmentsState>

    fun openDraftAttachment(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimetype: String
    )

    suspend fun openAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment)

    suspend fun preloadAttachment(attachment: Attachment): Option<URI>

    suspend fun shareAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment)

    suspend fun uploadNewAttachment(fileMetadata: FileMetadata)

    fun onClearAttachments()

    fun observeNewAttachments(onNewAttachment: (DraftAttachment) -> Unit): Flow<DraftAttachment>

    fun observeHasDeletedAttachments(onAttachmentDeleted: () -> Unit): Flow<Unit>

    fun observeHasRenamedAttachments(onAttachmentRenamed: () -> Unit): Flow<Unit>

    suspend fun getAttachmentsForItem(shareId: ShareId, itemId: ItemId)
}
