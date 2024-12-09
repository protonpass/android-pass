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

package proton.android.pass.features.attachments.mediapicker.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.notifications.api.SnackbarDispatcher
import java.net.URI
import javax.inject.Inject

@HiltViewModel
class MediaPickerViewModel @Inject constructor(
    private val draftAttachmentRepository: DraftAttachmentRepository,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    fun onFilePicked(uri: Uri) = viewModelScope.launch {
        draftAttachmentRepository.add(URI.create(uri.toString()))
    }

    fun onMediaPickerError(message: MediaPickerSnackbarMessage) = viewModelScope.launch {
        snackbarDispatcher(message)
    }
}
