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

package proton.android.pass.features.migrate.confirmvault

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.component.ProtonDialogTitle
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.composecomponents.impl.dialogs.NoPaddingDialog
import proton.android.pass.composecomponents.impl.dialogs.WarningSharedItemDialog
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.migrate.MigrateNavigation
import proton.android.pass.features.migrate.R

@Composable
fun MigrateConfirmVaultBottomSheet(
    modifier: Modifier = Modifier,
    navigation: (MigrateNavigation) -> Unit,
    viewModel: MigrateConfirmVaultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        val event = state.event
        if (event is Some) {
            when (val value = event.value) {
                is ConfirmMigrateEvent.ItemMigrated ->
                    navigation(MigrateNavigation.ItemMigrated(value.shareId, value.itemId))
                ConfirmMigrateEvent.AllItemsMigrated ->
                    navigation(MigrateNavigation.VaultMigrated)
                ConfirmMigrateEvent.Close ->
                    navigation(MigrateNavigation.DismissBottomsheet)
                ConfirmMigrateEvent.FolderMoved ->
                    navigation(MigrateNavigation.FolderMoved)
            }
        }
    }

    var showWarningVaultSharedDialog by rememberSaveable { mutableStateOf(false) }

    MigrateConfirmVaultContents(
        modifier = modifier
            .bottomSheet(horizontalPadding = PassTheme.dimens.bottomsheetHorizontalPadding),
        state = state,
        onVaultSelected = { viewModel.onVaultSelected(it) },
        onFolderSelected = { shareId, folderId -> viewModel.onFolderSelected(shareId, folderId) },
        onCancel = { viewModel.onCancel() },
        onConfirm = {
            if (state.canDisplayWarningVaultSharedDialog) {
                showWarningVaultSharedDialog = true
            } else {
                viewModel.onConfirm()
            }
        }
    )

    if (showWarningVaultSharedDialog) {
        WarningSharedItemDialog(
            description = proton.android.pass.composecomponents.impl.R.string.warning_dialog_item_shared_vault_moving,
            onOkClick = { reminderCheck ->
                showWarningVaultSharedDialog = false
                if (reminderCheck) viewModel.doNotDisplayWarningDialog()
                viewModel.onConfirm()
            },
            onCancelClick = { showWarningVaultSharedDialog = false }
        )
    }

    if (state.showDissolveFolderDialog) {
        NoPaddingDialog(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            backgroundColor = PassTheme.colors.backgroundWeak,
            onDismissRequest = { viewModel.onDismissDissolveFolderDialog() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.large),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
            ) {
                ProtonDialogTitle(
                    modifier = Modifier.padding(top = Spacing.large, bottom = Spacing.medium),
                    title = stringResource(R.string.migrate_dissolve_folder_dialog_title)
                )
                Text.Body1Regular(
                    text = stringResource(R.string.migrate_dissolve_folder_dialog_message)
                )
                DialogCancelConfirmSection(
                    modifier = Modifier.padding(vertical = Spacing.medium),
                    confirmText = stringResource(R.string.migrate_dissolve_folder_dialog_confirm),
                    onDismiss = { viewModel.onDismissDissolveFolderDialog() },
                    onConfirm = { viewModel.onConfirmDissolveFolder() },
                    color = PassTheme.colors.interactionNormMajor2
                )
            }
        }
    }
}
