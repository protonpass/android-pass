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

package proton.android.pass.features.attachments.mediapicker.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import proton.android.pass.features.attachments.mediapicker.navigation.MediaPickerNavigation
import proton.android.pass.features.attachments.mediapicker.presentation.MediaPickerSnackbarMessage.CouldNotOpenMediaPicker
import proton.android.pass.features.attachments.mediapicker.presentation.MediaPickerViewModel
import proton.android.pass.log.api.PassLogger

@Composable
fun MediaPickerScreen(
    modifier: Modifier = Modifier,
    onNavigate: (MediaPickerNavigation) -> Unit,
    viewmodel: MediaPickerViewModel = hiltViewModel()
) {
    val pickMedia = rememberLauncherForActivityResult(PickVisualMedia()) {
        val uri: Uri = it ?: return@rememberLauncherForActivityResult run {
            onNavigate(MediaPickerNavigation.Close)
        }
        viewmodel.onMediaPicked(uri)
        onNavigate(MediaPickerNavigation.Close)
    }
    LaunchedEffect(Unit) {
        runCatching {
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageAndVideo))
        }.onFailure {
            PassLogger.w(TAG, it)
            PassLogger.w(TAG, "Error launching picker")
            viewmodel.onMediaPickerError(CouldNotOpenMediaPicker)
            onNavigate(MediaPickerNavigation.Close)
        }
    }
    Box(modifier.fillMaxSize()) // workaround to avoid size animation
}

private const val TAG = "MediaPickerScreen"
