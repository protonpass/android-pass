/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.features.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.ProtonDialogTitle
import proton.android.pass.commonui.api.BrowserUtils.openWebsite
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.composecomponents.impl.text.Text

@Suppress("ComplexMethod")
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigate: (SettingsNavigation) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAttachmentConfigDialog by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        onNavigate(SettingsNavigation.AttachmentSyncDialog)
    }

    if (showNotificationSettingsDialog) {
        NoPaddingDialog(
            onDismissRequest = {
                showNotificationSettingsDialog = false
                onNavigate(SettingsNavigation.AttachmentSyncDialog)
            }
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = Spacing.mediumLarge,
                    vertical = Spacing.medium
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
            ) {
                ProtonDialogTitle(
                    title = stringResource(R.string.notification_permission_dialog_title)
                )

                Text.Body1Regular(
                    text = stringResource(R.string.notification_permission_dialog_message)
                )

                DialogCancelConfirmSection(
                    cancelText = stringResource(R.string.notification_permission_dialog_cancel),
                    confirmText = stringResource(R.string.notification_permission_dialog_open_settings),
                    onDismiss = {
                        showNotificationSettingsDialog = false
                        onNavigate(SettingsNavigation.AttachmentSyncDialog)
                    },
                    onConfirm = {
                        showNotificationSettingsDialog = false
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }.also(context::startActivity)
                        onNavigate(SettingsNavigation.AttachmentSyncDialog)
                    }
                )
            }
        }
    }

    if (showAttachmentConfigDialog) {
        proton.android.pass.features.settings.attachmentconfig.AttachmentConfigDialog(
            onDismiss = {
                showAttachmentConfigDialog = false
                viewModel.onDownloadAllAttachmentsToggled(isEnabled = false)
            },
            onStartDownload = {
                showAttachmentConfigDialog = false
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val isGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (isGranted) {
                        onNavigate(SettingsNavigation.AttachmentSyncDialog)
                    } else {
                        val activity = context.findActivity()
                        val shouldShowRationale = activity != null &&
                            ActivityCompat.shouldShowRequestPermissionRationale(
                                activity,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        if (shouldShowRationale) {
                            showNotificationSettingsDialog = true
                        } else {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        }
                    }
                } else {
                    onNavigate(SettingsNavigation.AttachmentSyncDialog)
                }
            }
        )
    }

    LaunchedEffect(state.event) {
        when (val event = state.event) {
            SettingsEvent.RestartApp -> {
                onNavigate(SettingsNavigation.Restart)
                viewModel.onEventConsumed(event)
            }

            SettingsEvent.OpenAttachmentConfigDialog -> {
                showAttachmentConfigDialog = true
                viewModel.onEventConsumed(event)
            }

            SettingsEvent.Unknown -> Unit
        }
    }

    LaunchedEffect(state.isForceRefreshing) {
        if (state.isForceRefreshing) {
            onNavigate(SettingsNavigation.SyncDialog)
        }
    }

    SettingsContent(
        modifier = modifier,
        state = state,
        onEvent = {
            when (it) {
                is SettingsContentEvent.UseFaviconsChange -> viewModel.onUseFaviconsChange(it.value)
                is SettingsContentEvent.UseDigitalAssetLinksChange ->
                    viewModel.onUseDigitalAssetLinksChange(it.value)
                is SettingsContentEvent.AllowScreenshotsChange ->
                    viewModel.onAllowScreenshotsChange(it.value)
                is SettingsContentEvent.TelemetryChange -> viewModel.onTelemetryChange(it.value)
                is SettingsContentEvent.CrashReportChange -> viewModel.onCrashReportChange(it.value)
                SettingsContentEvent.ViewLogs -> onNavigate(SettingsNavigation.ViewLogs)
                SettingsContentEvent.ForceSync -> viewModel.onForceSync()
                SettingsContentEvent.SelectTheme -> onNavigate(SettingsNavigation.SelectTheme)
                SettingsContentEvent.Clipboard -> onNavigate(SettingsNavigation.ClipboardSettings)
                SettingsContentEvent.Privacy -> { openWebsite(context, "https://proton.me/legal/privacy") }
                SettingsContentEvent.Terms -> { openWebsite(context, "https://proton.me/legal/terms") }
                SettingsContentEvent.Up -> onNavigate(SettingsNavigation.CloseScreen)
                is SettingsContentEvent.OnDisplayUsernameToggled -> {
                    viewModel.onToggleDisplayUsernameField(isEnabled = it.isEnabled)
                }

                is SettingsContentEvent.OnDisplayAutofillPinningToggled -> {
                    viewModel.onToggleDisplayAutofillPinning(isEnabled = it.isEnabled)
                }

                SettingsContentEvent.SelectAutofillDisplay ->
                    onNavigate(SettingsNavigation.SelectAutofillDisplay)

                is SettingsContentEvent.OnAutosaveChange -> {
                    viewModel.onAutosaveChange(isEnabled = it.isEnabled)
                }

                is SettingsContentEvent.OnDownloadAllAttachmentsToggled -> {
                    viewModel.onDownloadAllAttachmentsToggled(isEnabled = it.isEnabled)
                }

                is SettingsContentEvent.OnSharedItemsToggled -> {
                    viewModel.onSharedItemsToggled(isEnabled = it.isEnabled)
                }

                is SettingsContentEvent.OnAllowCellularDownloadToggled -> {
                    viewModel.onAllowCellularDownloadToggled(isEnabled = it.isEnabled)
                }

                SettingsContentEvent.OnOpenDownloadStatus -> {
                    onNavigate(SettingsNavigation.AttachmentSyncDialog)
                }

                SettingsContentEvent.OnOfflineAttachmentsUpsell -> {
                    onNavigate(SettingsNavigation.OfflineAttachmentsUpsell)
                }
            }
        }
    )
}

private fun Context.findActivity(): Activity? {
    var current = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}
