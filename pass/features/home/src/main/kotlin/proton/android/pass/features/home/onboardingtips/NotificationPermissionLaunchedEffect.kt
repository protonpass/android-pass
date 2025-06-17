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

package proton.android.pass.features.home.onboardingtips

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import proton.android.pass.log.api.PassLogger

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationPermissionLaunchedEffect(
    shouldRequestPermissions: Boolean,
    onPermissionRequested: () -> Unit,
    onPermissionChanged: (Boolean) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState =
            rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
        val status = permissionState.status
        val activity = LocalActivity.current

        LaunchedEffect(status.isGranted) {
            onPermissionChanged(status.isGranted)
        }

        LaunchedEffect(status, shouldRequestPermissions) {
            when {
                status.isGranted -> {}
                !status.shouldShowRationale && shouldRequestPermissions -> {
                    permissionState.launchPermissionRequest()
                    onPermissionRequested()
                }

                status.shouldShowRationale && shouldRequestPermissions -> {
                    try {
                        activity?.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", activity.packageName, null)
                            )
                        )
                        onPermissionRequested()
                    } catch (e: ActivityNotFoundException) {
                        PassLogger.d(TAG, e, "Settings not found")
                    }
                }
            }
        }
    }
}

private const val TAG = "OnBoardingTips"
