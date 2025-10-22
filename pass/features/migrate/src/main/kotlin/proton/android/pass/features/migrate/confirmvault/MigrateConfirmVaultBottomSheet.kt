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

package proton.android.pass.features.migrate.confirmvault

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.dialogs.WarningSharedItemDialog
import proton.android.pass.features.migrate.MigrateNavigation

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
                is ConfirmMigrateEvent.ItemMigrated -> {
                    navigation(MigrateNavigation.ItemMigrated(value.shareId, value.itemId))
                }

                is ConfirmMigrateEvent.AllItemsMigrated -> {
                    navigation(MigrateNavigation.VaultMigrated)
                }

                ConfirmMigrateEvent.Close -> navigation(MigrateNavigation.DismissBottomsheet)
            }
        }
    }

    var showWarningVaultSharedDialog by rememberSaveable { mutableStateOf(false) }

    MigrateConfirmVaultContents(
        modifier = modifier
            .bottomSheet(horizontalPadding = PassTheme.dimens.bottomsheetHorizontalPadding),
        state = state,
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
                if (reminderCheck) {
                    viewModel.doNotDisplayWarningDialog()
                }
                viewModel.onConfirm()

            },
            onCancelClick = {
                showWarningVaultSharedDialog = false
            }
        )
    }
}
