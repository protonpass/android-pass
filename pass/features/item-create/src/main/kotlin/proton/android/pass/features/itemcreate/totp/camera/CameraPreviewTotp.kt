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

package proton.android.pass.features.itemcreate.totp.camera

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import proton.android.pass.log.api.PassLogger

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewTotp(
    modifier: Modifier = Modifier,
    onUriReceived: (String) -> Unit,
    onOpenImagePicker: () -> Unit,
    onClosePreview: () -> Unit
) {
    val cameraPermissionState: PermissionState =
        rememberPermissionState(android.Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        CameraPreviewContent(
            modifier = modifier,
            onOpenImagePicker = onOpenImagePicker,
            onSuccess = onUriReceived,
            onDismiss = onClosePreview
        )
    } else {
        val activity = LocalActivity.current
        CameraPermissionContent(
            modifier = modifier,
            onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
            onOpenAppSettings = {
                try {
                    activity?.startActivity(
                        Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", activity.packageName, null)
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    PassLogger.d(TAG, e, "Settings not found")
                }
            },
            onDismiss = onClosePreview
        )
    }
}

private const val TAG = "CameraPreviewTotp"
