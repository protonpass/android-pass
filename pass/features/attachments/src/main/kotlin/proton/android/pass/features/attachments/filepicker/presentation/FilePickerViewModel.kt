/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.attachments.filepicker.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.biometry.AuthOverrideState
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.domain.attachments.DraftAttachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.notifications.api.SnackbarDispatcher
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class FilePickerViewModel @Inject constructor(
    private val draftAttachmentRepository: DraftAttachmentRepository,
    private val metadataResolver: MetadataResolver,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val authOverrideState: AuthOverrideState
) : ViewModel() {

    private val eventFlow = MutableStateFlow<FilePickerEvent>(FilePickerEvent.Idle)
    val state = eventFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FilePickerEvent.Idle
    )

    fun onFilePicked(contentUri: Uri) {
        authOverrideState.setAuthOverride(false)
        viewModelScope.launch {
            val uri = URI.create(contentUri.toString())
            val fileMetadata = metadataResolver.extractMetadata(uri) ?: FileMetadata.unknown(uri)
            val draftAttachment = DraftAttachment.Loading(fileMetadata)
            draftAttachmentRepository.add(draftAttachment)
            eventFlow.update { FilePickerEvent.Close }
        }
    }

    fun onCloseFilePicker(message: FilePickerSnackbarMessage? = null) {
        authOverrideState.setAuthOverride(false)
        viewModelScope.launch {
            message?.let { snackbarDispatcher(it) }
            eventFlow.update { FilePickerEvent.Close }
        }
    }

    fun onConsumeEvent(event: FilePickerEvent) {
        eventFlow.compareAndSet(event, FilePickerEvent.Idle)
    }

    fun onOpenFilePicker() {
        authOverrideState.setAuthOverride(true)
    }
}
