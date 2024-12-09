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

package proton.android.pass.features.attachments.camera.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import proton.android.pass.features.attachments.camera.navigation.CameraNavigation
import proton.android.pass.features.attachments.camera.presentation.CameraSnackbarMessage.CouldNotOpenCamera
import proton.android.pass.features.attachments.camera.presentation.CameraViewModel
import proton.android.pass.log.api.PassLogger

@Composable
fun CameraScreen(onNavigate: (CameraNavigation) -> Unit, viewmodel: CameraViewModel = hiltViewModel()) {
    val pickMedia = rememberLauncherForActivityResult(TakePicture()) { hasImage ->

    }
    LaunchedEffect(Unit) {
        runCatching {

        }.onFailure {
            PassLogger.w(TAG, it)
            PassLogger.w(TAG, "Error launching camera")
            viewmodel.onCameraError(CouldNotOpenCamera)
            onNavigate(CameraNavigation.Close)
        }
    }
}

private const val TAG = "CameraScreen"
