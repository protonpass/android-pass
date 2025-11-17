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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.itemcreate.R
import proton.android.pass.log.api.PassLogger

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewTotp(
    modifier: Modifier = Modifier,
    onUriReceived: (String) -> Unit,
    onOpenImagePicker: () -> Unit,
    onClosePreview: () -> Unit,
    viewModel: CameraPreviewTotpViewModel = hiltViewModel()
) {
    if (viewModel.isQuest) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Circle(
                modifier = Modifier
                    .align(alignment = Alignment.TopStart)
                    .padding(start = Spacing.mediumLarge, top = Spacing.mediumLarge),
                backgroundColor = PassTheme.colors.textHint,
                onClick = onClosePreview
            ) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross_small),
                    contentDescription = stringResource(R.string.close_scree_icon_content_description),
                    tint = PassTheme.colors.textNorm
                )
            }

            Text.Hero(
                modifier = Modifier.align(alignment = Alignment.Center),
                text = stringResource(R.string.functionality_not_available)
            )
        }
    } else {
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
}

private const val TAG = "CameraPreviewTotp"
