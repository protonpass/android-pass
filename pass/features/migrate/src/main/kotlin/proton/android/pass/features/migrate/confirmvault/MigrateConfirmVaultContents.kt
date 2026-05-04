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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetCancelConfirm
import proton.android.pass.composecomponents.impl.container.PassInfoWarningBanner
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.migrate.R

@Composable
internal fun MigrateConfirmVaultContents(
    modifier: Modifier = Modifier,
    state: MigrateConfirmVaultUiState,
    onVaultSelected: (ShareId) -> Unit,
    onFolderSelected: (ShareId, FolderId) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val hasSelection = state.selectedShareId is Some
    val folderSelected = state.selectedFolderId !is None

    val title = when (state.mode) {
        is MigrateMode.MigrateSelectedItems -> when {
            hasSelection && folderSelected -> pluralStringResource(
                R.plurals.migrate_item_to_folder_confirm_title_bottom_sheet,
                state.mode.number,
                state.mode.number
            )
            hasSelection -> pluralStringResource(
                R.plurals.migrate_item_confirm_title_bottom_sheet,
                state.mode.number,
                state.mode.number
            )
            else -> stringResource(R.string.migrate_select_vault_title)
        }

        MigrateMode.MigrateAll -> stringResource(R.string.migrate_all_items_confirm_title_bottom_sheet)

        MigrateMode.MoveFolder -> if (folderSelected) {
            stringResource(R.string.migrate_folder_to_folder_confirm_title_bottom_sheet)
        } else {
            stringResource(R.string.migrate_folder_confirm_title_bottom_sheet)
        }

        MigrateMode.MoveAllItemsInFolder -> if (folderSelected) {
            stringResource(R.string.migrate_move_all_items_to_folder_confirm_title_bottom_sheet)
        } else {
            stringResource(R.string.migrate_move_all_items_confirm_title_bottom_sheet)
        }
    }

    Column(modifier = modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium, vertical = Spacing.medium),
            text = title,
            textAlign = TextAlign.Center,
            color = PassTheme.colors.textNorm
        )

        Column(
            modifier = Modifier.padding(horizontal = Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            if (hasSelection && !state.isSameVaultMove) {
                PassInfoWarningBanner(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.migrate_item_warning_history)
                )
            }

            if (hasSelection && state.hasAssociatedSecureLinks) {
                PassInfoWarningBanner(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.migrate_item_warning_secure_link)
                )
            }
        }

        MigrateVaultSelectorContents(
            modifier = Modifier.weight(1f),
            vaults = state.vaultList,
            folderIdToExpand = state.folderIdToExpand,
            selectedShareId = state.selectedShareId,
            selectedFolderId = state.selectedFolderId,
            disabledFolderId = state.disabledFolderId,
            disabledFolderItemCount = state.disabledFolderItemCount,
            onVaultSelected = onVaultSelected,
            onFolderSelected = onFolderSelected
        )

        BottomSheetCancelConfirm(
            isLoading = state.isLoading.value(),
            confirmEnabled = hasSelection,
            confirmText = stringResource(R.string.migrate_item_confirm_confirm_button),
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}
