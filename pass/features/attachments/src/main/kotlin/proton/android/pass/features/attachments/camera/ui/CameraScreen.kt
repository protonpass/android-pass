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

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.TakePicture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.attachments.R
import proton.android.pass.features.attachments.camera.navigation.CameraNavigation
import proton.android.pass.features.attachments.camera.presentation.CameraSnackbarMessage.CouldNotOpenCamera
import proton.android.pass.features.attachments.camera.presentation.CameraViewModel
import proton.android.pass.log.api.PassLogger

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    onNavigate: (CameraNavigation) -> Unit,
    viewmodel: CameraViewModel = hiltViewModel()
) {
    val activity = LocalContext.current as Activity
    var openCameraLauncher by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    val cameraPermissionState: PermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCameraLauncher = true
        } else {
            onNavigate(CameraNavigation.Close)
        }
    }

    LaunchedEffect(cameraPermissionState) {
        if (!cameraPermissionState.status.isGranted && cameraPermissionState.status.shouldShowRationale) {
            showRationale = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    if (openCameraLauncher) {
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        val cameraLauncher = rememberLauncherForActivityResult(TakePicture()) { hasImage ->
            if (hasImage) {
                val safeUri = imageUri ?: return@rememberLauncherForActivityResult run {
                    PassLogger.w(TAG, "Image uri is null")
                    onNavigate(CameraNavigation.Close)
                }
                viewmodel.onPhotoTaken(safeUri)
                onNavigate(CameraNavigation.Close)
            } else {
                onNavigate(CameraNavigation.Close)
            }
        }
        LaunchedEffect(Unit) {
            runCatching {
                viewmodel.createTempUri {
                    imageUri = it
                    cameraLauncher.launch(it)
                }
            }.onFailure {
                PassLogger.w(TAG, "Error launching camera")
                PassLogger.w(TAG, it)
                viewmodel.onCameraError(CouldNotOpenCamera)
                onNavigate(CameraNavigation.Close)
            }
        }
    }
    if (showRationale) {
        Box(modifier = modifier.fillMaxSize()) {
            SmallCrossIconButton(modifier = Modifier.align(Alignment.TopEnd)) {
                onNavigate(CameraNavigation.Close)
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(60.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text.Body1Regular(
                    modifier = Modifier.width(250.dp),
                    text = stringResource(R.string.camera_permission_explanation),
                    textAlign = TextAlign.Center
                )
                CircleButton(
                    modifier = Modifier.width(250.dp),
                    color = PassTheme.colors.loginInteractionNormMajor1,
                    onClick = {
                        runCatching {
                            activity.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", activity.packageName, null)
                                )
                            )
                        }.onFailure {
                            PassLogger.d(TAG, it, "Settings not found")
                        }
                    }
                ) {
                    Text.Body3Regular(
                        modifier = Modifier.width(250.dp),
                        text = stringResource(R.string.camera_permission_open_settings),
                        textAlign = TextAlign.Center,
                        color = PassTheme.colors.textInvert
                    )
                }
            }
        }
    } else {
        Box(modifier.fillMaxSize()) // workaround to avoid size animation
    }
}

private const val TAG = "CameraScreen"
