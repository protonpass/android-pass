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

package proton.android.pass.featurehome.impl.onboardingtips

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import proton.android.pass.log.api.PassLogger

@Composable
fun OnBoardingTips(
    modifier: Modifier = Modifier,
    onTrialInfoClick: () -> Unit,
    onInviteClick: () -> Unit,
    viewModel: OnBoardingTipsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    NotificationPermissionLaunchedEffect(
        shouldRequestPermissions = state.event == OnBoardingTipsEvent.RequestNotificationPermission,
        onPermissionRequested = viewModel::clearEvent,
        onPermissionChanged = viewModel::onNotificationPermissionChanged
    )

    LaunchedEffect(state.event) {
        when (state.event) {
            OnBoardingTipsEvent.OpenTrialScreen -> {
                onTrialInfoClick()
                viewModel.clearEvent()
            }

            OnBoardingTipsEvent.OpenInviteScreen -> {
                onInviteClick()
                viewModel.clearEvent()
            }

            OnBoardingTipsEvent.RequestNotificationPermission -> {}
            OnBoardingTipsEvent.Unknown -> {}
        }
    }

    OnBoardingTipContent(
        modifier = modifier,
        tipsSetToShow = state.tipsToShow,
        onClick = viewModel::onClick,
        onDismiss = viewModel::onDismiss
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun NotificationPermissionLaunchedEffect(
    shouldRequestPermissions: Boolean,
    onPermissionRequested: () -> Unit,
    onPermissionChanged: (Boolean) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionState = rememberPermissionState(permission = android.Manifest.permission.POST_NOTIFICATIONS)
        val status = permissionState.status
        val activity = LocalContext.current as Activity

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
                        activity.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", activity.packageName, null)
                            )
                        )
                    } catch (e: ActivityNotFoundException) {
                        PassLogger.d(TAG, e, "Settings not found")
                    }
                }
            }
        }
    }
}

private const val TAG = "OnBoardingTips"
