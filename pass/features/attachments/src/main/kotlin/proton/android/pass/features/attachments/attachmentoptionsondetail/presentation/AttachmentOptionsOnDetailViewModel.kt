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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.attachments.CheckIfAttachmentExistsLocally
import proton.android.pass.data.api.usecases.attachments.GetAttachment
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import javax.inject.Inject

@HiltViewModel
class AttachmentOptionsOnDetailViewModel @Inject constructor(
    private val getAttachment: GetAttachment,
    private val attachmentsHandler: AttachmentsHandler,
    checkIfAttachmentExistsLocally: CheckIfAttachmentExistsLocally,
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

    private val eventFlow =
        MutableStateFlow<AttachmentOptionsOnDetailEvent>(AttachmentOptionsOnDetailEvent.Idle)

    val state: StateFlow<AttachmentOptionsOnDetailState> = combine(
        eventFlow,
        oneShot { checkIfAttachmentExistsLocally(shareId, itemId, attachmentId) }
    ) { event, attachmentExists ->
        AttachmentOptionsOnDetailState(
            canDownload = !attachmentExists,
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

    fun download() {
        viewModelScope.launch {
            val attachment = getAttachment(shareId, itemId, attachmentId)
            attachmentsHandler.loadAttachment(attachment)
            eventFlow.update { AttachmentOptionsOnDetailEvent.Close }
        }
    }

    fun share(classHolder: ClassHolder<Context>) {
        viewModelScope.launch {
            val attachment = getAttachment(shareId, itemId, attachmentId)
            attachmentsHandler.shareAttachment(classHolder, attachment)
            eventFlow.update { AttachmentOptionsOnDetailEvent.Close }
        }
    }
}
