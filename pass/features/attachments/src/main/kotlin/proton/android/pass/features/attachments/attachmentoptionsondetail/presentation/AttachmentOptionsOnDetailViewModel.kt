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

package proton.android.pass.features.attachments.attachmentoptionsondetail.presentation

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.attachments.GetAttachment
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.features.attachments.attachmentoptionsondetail.presentation.AttachmentDetailOptionsSnackbarMessages.AttachmentSavedToLocationError
import proton.android.pass.features.attachments.attachmentoptionsondetail.presentation.AttachmentDetailOptionsSnackbarMessages.AttachmentSavedToLocationSuccess
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import java.io.IOException
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class AttachmentOptionsOnDetailViewModel @Inject constructor(
    private val getAttachment: GetAttachment,
    private val attachmentsHandler: AttachmentsHandler,
    private val appDispatchers: AppDispatchers,
    private val snackbarDispatcher: SnackbarDispatcher,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId = savedStateHandleProvider.get()
        .require<String>(CommonOptionalNavArgId.ShareId.key)
        .let(::ShareId)
    private val itemId: ItemId = savedStateHandleProvider.get()
        .require<String>(CommonOptionalNavArgId.ItemId.key)
        .let(::ItemId)
    private val attachmentId: AttachmentId = savedStateHandleProvider.get()
        .require<String>(CommonOptionalNavArgId.AttachmentId.key)
        .let(::AttachmentId)

    private val cachedFileUriFlow = MutableStateFlow<Option<URI>>(None)
    private val isDownloadingFlow =
        MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)
    private val isSharingFlow =
        MutableStateFlow<IsLoadingState>(IsLoadingState.NotLoading)

    private val eventFlow =
        MutableStateFlow<AttachmentOptionsOnDetailEvent>(AttachmentOptionsOnDetailEvent.Idle)

    val state: StateFlow<AttachmentOptionsOnDetailState> = combine(
        isDownloadingFlow,
        isSharingFlow,
        eventFlow
    ) { isSavingToLocation, isSharing, event ->
        AttachmentOptionsOnDetailState(
            isSavingToLocation = isSavingToLocation.value(),
            isSharing = isSharing.value(),
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
        initialValue = AttachmentOptionsOnDetailState.Initial
    )

    fun onConsumeEvent(event: AttachmentOptionsOnDetailEvent) {
        eventFlow.compareAndSet(event, AttachmentOptionsOnDetailEvent.Idle)
    }

    fun saveToLocation() {
        viewModelScope.launch {
            isDownloadingFlow.update { IsLoadingState.Loading }
            val attachment = getAttachment(shareId, itemId, attachmentId)
            val uri = attachmentsHandler.preloadAttachment(attachment)
            if (uri is Some) {
                cachedFileUriFlow.update { uri }
                eventFlow.update {
                    AttachmentOptionsOnDetailEvent.SaveToLocation(
                        fileName = attachment.name,
                        mimeType = attachment.mimeType
                    )
                }
                isDownloadingFlow.update { IsLoadingState.NotLoading }
            } else {
                eventFlow.update { AttachmentOptionsOnDetailEvent.Close }
            }
        }
    }

    fun share(classHolder: ClassHolder<Context>) {
        viewModelScope.launch {
            isSharingFlow.update { IsLoadingState.Loading }
            val attachment = getAttachment(shareId, itemId, attachmentId)
            attachmentsHandler.shareAttachment(classHolder, attachment)
            isSharingFlow.update { IsLoadingState.NotLoading }
            eventFlow.update { AttachmentOptionsOnDetailEvent.Close }
        }
    }

    @Suppress("ThrowsCount")
    fun copyFile(classHolder: ClassHolder<Context>, toUri: Option<Uri>) {
        viewModelScope.launch {
            runCatching {
                val fromUri = cachedFileUriFlow.value.map { uri -> uri.toString().toUri() }
                val context = classHolder.get().value()
                    ?: throw IllegalStateException("Context is not available")
                if (fromUri is Some && toUri is Some) {
                    withContext(appDispatchers.io) {
                        context.contentResolver.openInputStream(fromUri.value)?.use { inputStream ->
                            context.contentResolver.openOutputStream(toUri.value)
                                ?.use { outputStream ->
                                    withContext(NonCancellable) {
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                                ?: throw IOException("Unable to open output stream for URI: $toUri")
                        } ?: throw IOException("Unable to open input stream for URI: $fromUri")
                    }
                } else {
                    throw IllegalStateException("Invalid URIs")
                }
            }.onFailure {
                PassLogger.w(TAG, "Failed to copy file")
                PassLogger.w(TAG, it)
                snackbarDispatcher(AttachmentSavedToLocationError)
            }.onSuccess {
                snackbarDispatcher(AttachmentSavedToLocationSuccess)
            }
            eventFlow.update { AttachmentOptionsOnDetailEvent.Close }
        }
    }

    companion object {
        private const val TAG = "AttachmentOptionsOnDetailViewModel"
    }
}
