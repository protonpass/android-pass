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

package proton.android.pass.features.attachments.renameattachment.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.usecases.attachments.RenameAttachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class RenameAttachmentViewModel @Inject constructor(
    private val renameAttachment: RenameAttachment,
    private val draftAttachmentRepository: DraftAttachmentRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val attachmentId: Option<AttachmentId> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.AttachmentId.key)
        ?.let(::AttachmentId)
        .toOption()

    private val uri: Option<URI> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.Uri.key)
        .toOption()
        .map(NavParamEncoder::decode)
        .map(URI::create)

    @OptIn(SavedStateHandleSaveableApi::class)
    var filename by savedStateHandleProvider.get()
        .saveable { mutableStateOf("") }

    private lateinit var initialFileName: String

    init {
        viewModelScope.launch {
            when {
                uri is Some -> {
                    val draftAttachment = draftAttachmentRepository.get(uri.value)
                    initialFileName = draftAttachment.metadata.name
                    filename = initialFileName
                }

                attachmentId is Some -> {
                    // To implement
                }

                else -> {
                    eventFlow.update { RenameAttachmentEvent.Close }
                }
            }
        }
    }

    private val eventFlow = MutableStateFlow<RenameAttachmentEvent>(RenameAttachmentEvent.Idle)

    val state = eventFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = RenameAttachmentEvent.Idle
        )

    fun onConsumeEvent(event: RenameAttachmentEvent) {
        eventFlow.compareAndSet(event, RenameAttachmentEvent.Idle)
    }

    fun onValueChange(value: String) {
        filename = value
    }

    fun onConfirm() {
        if (initialFileName != filename) {
            viewModelScope.launch {
                runCatching {
                    when {
                        attachmentId is Some -> renameAttachment(attachmentId.value, filename)
                        uri is Some -> renameAttachment(uri.value, filename)
                    }
                }.onSuccess {
                    PassLogger.i(TAG, "Attachment renamed")
                    eventFlow.update { RenameAttachmentEvent.Close }
                }.onFailure {
                    PassLogger.w(TAG, "Failed to rename attachment")
                    PassLogger.w(TAG, it)
                }
            }
        }
    }
    companion object {
        private const val TAG = "RenameAttachmentViewModel"
    }
}

