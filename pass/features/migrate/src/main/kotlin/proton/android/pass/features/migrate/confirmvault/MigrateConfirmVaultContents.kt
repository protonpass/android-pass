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
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetCancelConfirm
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.bottomSheetDivider
import proton.android.pass.composecomponents.impl.container.PassInfoWarningBanner
import proton.android.pass.features.migrate.R

@Composable
internal fun MigrateConfirmVaultContents(
    modifier: Modifier = Modifier,
    state: MigrateConfirmVaultUiState,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val title = when (state.mode) {
        is MigrateMode.MigrateSelectedItems -> pluralStringResource(
            R.plurals.migrate_item_confirm_title_bottom_sheet,
            state.mode.number,
            state.mode.number
        )

        MigrateMode.MigrateAll -> stringResource(R.string.migrate_all_items_confirm_title_bottom_sheet)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = Spacing.medium)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
        ) {
            PassInfoWarningBanner(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = stringResource(id = R.string.migrate_item_warning_history)
            )

            if (state.hasAssociatedSecureLinks) {
                PassInfoWarningBanner(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(id = R.string.migrate_item_warning_secure_link)
                )
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.medium),
            text = title,
            textAlign = TextAlign.Center,
            color = PassTheme.colors.textNorm
        )

        if (state.vault is Some) {
            BottomSheetItemList(
                items = persistentListOf(
                    bottomSheetDivider(),
                    BottomSheetVaultRow(
                        vault = state.vault.value,
                        isSelected = false,
                        onVaultClick = null
                    ),
                    bottomSheetDivider()
                )
            )
        }

        BottomSheetCancelConfirm(
            isLoading = state.isLoading.value(),
            confirmText = stringResource(R.string.migrate_item_confirm_confirm_button),
            onCancel = onCancel,
            onConfirm = onConfirm
        )
    }
}
