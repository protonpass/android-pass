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

package proton.android.pass.featureitemcreate.impl.common.attachments

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.api.usecases.attachments.ClearAttachments
import proton.android.pass.data.api.usecases.attachments.UploadAttachment
import proton.android.pass.log.api.PassLogger
import java.net.URI
import javax.inject.Inject

@ViewModelScoped
class AttachmentsHandlerImpl @Inject constructor(
    private val draftAttachmentRepository: DraftAttachmentRepository,
    private val metadataResolver: MetadataResolver,
    private val uploadAttachment: UploadAttachment,
    private val clearAttachmentsCallback: ClearAttachments
) : AttachmentsHandler {

    private val isUploadingAttachmentState: MutableStateFlow<Set<URI>> =
        MutableStateFlow(emptySet())
    override val isUploadingAttachment: StateFlow<Set<URI>> get() = isUploadingAttachmentState

    private val draftAttachmentsFlow = draftAttachmentRepository.observeAll()
        .map { uris -> uris.mapNotNull { metadataResolver.extractMetadata(it) } }

    override val attachmentsFlow: Flow<AttachmentsState> = combine(
        isUploadingAttachmentState,
        draftAttachmentsFlow
    ) { loadingAttachments, draftAttachmentsList ->
        AttachmentsState(
            loadingDraftAttachments = loadingAttachments,
            draftAttachmentsList = draftAttachmentsList,
            attachmentsList = emptyList(),
            loadingAttachments = emptySet()
        )
    }.distinctUntilChanged()

    override fun uploadNewAttachment(uri: URI, scope: CoroutineScope) {
        isUploadingAttachmentState.update { it + uri }
        scope.launch {
            runCatching { uploadAttachment(uri) }
                .onFailure {
                    PassLogger.w(TAG, "Could not upload attachment: $uri")
                    PassLogger.w(TAG, it)
                }
        }
        isUploadingAttachmentState.update { it - uri }
    }

    override fun clearAttachments() {
        clearAttachmentsCallback()
    }

    override fun observeNewAttachments(scope: CoroutineScope, onNewAttachment: (Set<URI>) -> Unit) {
        draftAttachmentRepository.observeNew()
            .onEach { newUris ->
                if (newUris.isNotEmpty()) onNewAttachment(newUris)
            }
            .launchIn(scope)
    }

    companion object {
        private const val TAG = "DefaultAttachmentsHandler"
    }
}
