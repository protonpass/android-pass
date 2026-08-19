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

package proton.android.pass.commonpresentation.fakes.attachments

import android.content.Context
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.DraftAttachment
import proton.android.pass.domain.attachments.FileMetadata
import java.net.URI

@Suppress("TooManyFunctions")
class FakeAttachmentHandler : AttachmentsHandler {

    private val newAttachmentsFlow = MutableSharedFlow<DraftAttachment>(extraBufferCapacity = BUFFER_CAPACITY)
    private var uploadGate: CompletableDeferred<Unit>? = null

    override val attachmentState: Flow<AttachmentsState>
        get() = flowOf(AttachmentsState.Initial)

    override suspend fun openDraftAttachment(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimetype: String
    ) = Unit

    override fun onClearAttachments() = Unit

    override fun observeNewAttachments(onNewAttachment: (DraftAttachment) -> Unit): Flow<DraftAttachment> =
        newAttachmentsFlow.onEach(onNewAttachment)

    override fun observeHasDeletedAttachments(onAttachmentDeleted: () -> Unit): Flow<Unit> = flowOf(Unit)

    override fun observeHasRenamedAttachments(onAttachmentRenamed: () -> Unit): Flow<Unit> = flowOf(Unit)

    override suspend fun getAttachmentsForItem(shareId: ShareId, itemId: ItemId) = Unit

    override suspend fun copyAttachmentsAsDraft(shareId: ShareId, itemId: ItemId) = Unit

    override suspend fun openAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment) = Unit

    override suspend fun preloadAttachment(attachment: Attachment): Option<URI> = None

    override suspend fun shareAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment) = Unit

    override suspend fun uploadNewAttachment(fileMetadata: FileMetadata) {
        uploadGate?.await()
    }

    fun emitNewAttachment(draftAttachment: DraftAttachment) {
        newAttachmentsFlow.tryEmit(draftAttachment)
    }

    fun blockUploads() {
        uploadGate = CompletableDeferred()
    }

    fun releaseUploads() {
        uploadGate?.complete(Unit)
        uploadGate = null
    }

    private companion object {
        private const val BUFFER_CAPACITY = 10
    }
}
