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

package proton.android.pass.featurevault.impl.bottomsheet.options

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.feature.vault.impl.R
import me.proton.core.presentation.R as CoreR

@ExperimentalMaterialApi
@Composable
fun VaultOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    state: VaultOptionsUiState.Success,
    onEdit: () -> Unit,
    onMigrate: () -> Unit,
    onRemove: () -> Unit,
    onShare: () -> Unit
) {
    val items = mutableListOf<BottomSheetItem>()
    if (state.showEdit) {
        items.add(editVault(onEdit))
    }
    if (state.showMigrate) {
        items.add(migrateVault(onMigrate))
    }
    if (state.showShare) {
        items.add(shareVault(onShare))
    }
    if (state.showDelete) {
        items.add(removeVault(onRemove))
    }

    Column(modifier.bottomSheet()) {
        BottomSheetItemList(
            items = if (items.size > 1) {
                items.withDividers()
            } else {
                items
            }.toPersistentList()
        )
    }
}

private fun editVault(onEdit: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_edit)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_pencil) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onEdit() }
        override val isDivider = false
    }

private fun migrateVault(onMigrate: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = { BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_migrate)) }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = { BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_folder_arrow_in) }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onMigrate() }
        override val isDivider = false
    }

private fun removeVault(onRemove: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(
                    text = stringResource(id = R.string.bottomsheet_delete_vault),
                    color = ProtonTheme.colors.notificationError
                )
            }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = {
                BottomSheetItemIcon(
                    iconId = CoreR.drawable.ic_proton_trash_cross,
                    tint = ProtonTheme.colors.notificationError
                )
            }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onRemove() }
        override val isDivider = false
    }

private fun shareVault(onClick: () -> Unit): BottomSheetItem =
    object : BottomSheetItem {
        override val title: @Composable () -> Unit
            get() = {
                BottomSheetItemTitle(text = stringResource(id = R.string.bottomsheet_share_vault))
            }
        override val subtitle: (@Composable () -> Unit)?
            get() = null
        override val leftIcon: (@Composable () -> Unit)
            get() = {
                BottomSheetItemIcon(iconId = CoreR.drawable.ic_proton_user_plus)
            }
        override val endIcon: (@Composable () -> Unit)?
            get() = null
        override val onClick: () -> Unit
            get() = { onClick() }
        override val isDivider = false
    }

class ThemeVaultOptionsInput :
    ThemePairPreviewProvider<VaultOptionsUiState.Success>(
        VaultOptionsBottomSheetContentsPreviewProvider()
    )

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun VaultOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemeVaultOptionsInput::class) input: Pair<Boolean, VaultOptionsUiState.Success>
) {
    PassTheme(isDark = input.first) {
        Surface {
            VaultOptionsBottomSheetContents(
                state = input.second,
                onEdit = {},
                onMigrate = {},
                onRemove = {},
                onShare = {},
            )
        }
    }
}
