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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.attachments.filepicker.navigation.FilePickerNavigation
import proton.android.pass.features.attachments.filepicker.presentation.FilePickerEvent
import proton.android.pass.features.attachments.filepicker.presentation.FilePickerSnackbarMessage.CouldNotOpenFilePicker
import proton.android.pass.features.attachments.filepicker.presentation.FilePickerViewModel
import proton.android.pass.log.api.PassLogger

@Composable
fun FilePickerScreen(
    modifier: Modifier = Modifier,
    onNavigate: (FilePickerNavigation) -> Unit,
    viewmodel: FilePickerViewModel = hiltViewModel()
) {
    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        val uri: Uri = it ?: return@rememberLauncherForActivityResult run {
            viewmodel.onCloseFilePicker()
        }
        viewmodel.onFilePicked(uri)
    }
    val state by viewmodel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            FilePickerEvent.Close -> onNavigate(FilePickerNavigation.Close)
            FilePickerEvent.Idle -> {}
        }
        viewmodel.onConsumeEvent(state)
    }

    LaunchedEffect(Unit) {
        runCatching {
            viewmodel.onOpenFilePicker()
            pickFile.launch("*/*")
        }.onFailure {
            PassLogger.w(TAG, it)
            PassLogger.w(TAG, "Error launching picker")
            viewmodel.onCloseFilePicker(CouldNotOpenFilePicker)
        }
    }
    Box(modifier.fillMaxSize()) // workaround to avoid size animation
}

private const val TAG = "FilePickerScreen"
