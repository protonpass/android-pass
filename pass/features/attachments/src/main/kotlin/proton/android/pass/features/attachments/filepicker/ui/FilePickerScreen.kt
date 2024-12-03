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

package proton.android.pass.features.attachments.filepicker.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import proton.android.pass.features.attachments.filepicker.navigation.FilePickerNavigation
import proton.android.pass.features.attachments.filepicker.presentation.FilePickerSnackbarMessage.CouldNotOpenFilePicker
import proton.android.pass.features.attachments.filepicker.presentation.FilePickerSnackbarMessage.CouldNotSelectFile
import proton.android.pass.features.attachments.filepicker.presentation.FilePickerViewModel
import proton.android.pass.log.api.PassLogger

@Composable
fun FilePickerScreen(onNavigate: (FilePickerNavigation) -> Unit, viewmodel: FilePickerViewModel = hiltViewModel()) {
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        val uri: Uri = it ?: return@rememberLauncherForActivityResult run {
            viewmodel.onFilePickerError(CouldNotSelectFile)
            onNavigate(FilePickerNavigation.Close)
        }
        viewmodel.onFilePicked(uri)
        onNavigate(FilePickerNavigation.Close)
    }
    LaunchedEffect(Unit) {
        runCatching {
            pickFile.launch("*/*")
        }.onFailure {
            PassLogger.w(TAG, it)
            PassLogger.w(TAG, "Error launching picker")
            viewmodel.onFilePickerError(CouldNotOpenFilePicker)
            onNavigate(FilePickerNavigation.Close)
        }
    }
}

private const val TAG = "FilePickerScreen"
