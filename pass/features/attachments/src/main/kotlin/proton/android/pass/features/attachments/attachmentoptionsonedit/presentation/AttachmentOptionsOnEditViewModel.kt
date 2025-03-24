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

package proton.android.pass.features.attachments.attachmentoptionsonedit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.data.api.usecases.attachments.RemoveDraftAttachment
import proton.android.pass.data.api.usecases.attachments.SetAttachmentToBeUnlinked
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class AttachmentOptionsOnEditViewModel @Inject constructor(
    private val setAttachmentToBeUnlinked: SetAttachmentToBeUnlinked,
    private val removeDraftAttachment: RemoveDraftAttachment,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: Option<ShareId> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.ShareId.key)
        ?.let(::ShareId)
        .toOption()
    private val itemId: Option<ItemId> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.ItemId.key)
        ?.let(::ItemId)
        .toOption()
    private val attachmentId: Option<AttachmentId> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.AttachmentId.key)
        ?.let(::AttachmentId)
        .toOption()

    private val uri: Option<URI> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.Uri.key)
        .toOption()
        .map(NavParamEncoder::decode)
        .map(URI::create)

    private val eventFlow = MutableStateFlow<AttachmentOptionsOnEditEvent>(AttachmentOptionsOnEditEvent.Idle)

    val state = eventFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = AttachmentOptionsOnEditEvent.Idle
        )

    fun deleteAttachment() {
        when {
            attachmentId is Some -> setAttachmentToBeUnlinked(attachmentId.value)
            uri is Some -> removeDraftAttachment(uri.value)
        }
        eventFlow.update { AttachmentOptionsOnEditEvent.Close }
    }

    fun renameAttachment() {
        when {
            shareId is Some && itemId is Some && attachmentId is Some -> eventFlow.update {
                AttachmentOptionsOnEditEvent.OpenRenameAttachment(
                    shareId = shareId.value,
                    itemId = itemId.value,
                    attachmentId = attachmentId.value
                )
            }

            uri is Some -> eventFlow.update {
                AttachmentOptionsOnEditEvent.OpenRenameDraftAttachment(uri.value)
            }
            else -> throw IllegalStateException("No attachment id or uri found")
        }
    }

    fun onConsumeEvent(event: AttachmentOptionsOnEditEvent) {
        eventFlow.compareAndSet(event, AttachmentOptionsOnEditEvent.Idle)
    }
}
