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

package proton.android.pass.features.home.vault

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.heroNorm
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.AllVaultsIcon
import proton.android.pass.composecomponents.impl.icon.TrashVaultIcon
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.features.home.R
import proton.android.pass.searchoptions.api.VaultSelectionOption

@Composable
fun VaultDrawerSection(
    modifier: Modifier = Modifier,
    homeVaultSelection: VaultSelectionOption,
    list: ImmutableList<ShareUiModelWithItemCount>,
    totalTrashedItems: Long,
    onVaultOptionsClick: (ShareUiModelWithItemCount) -> Unit,
    onAllVaultsClick: () -> Unit,
    onVaultClick: (ShareId) -> Unit = {},
    onTrashClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = modifier.fillMaxHeight()
    ) {
        item {
            Text(
                modifier = Modifier
                    .padding(horizontal = PassTheme.dimens.bottomsheetHorizontalPadding),
                text = stringResource(R.string.vault_drawer_vaults_title),
                color = PassTheme.colors.textNorm,
                style = PassTheme.typography.heroNorm()
            )
        }
        if (list.size > 1) {
            item {
                VaultDrawerRow(
                    name = stringResource(R.string.vault_drawer_all_vaults),
                    itemCount = list.sumOf { it.activeItemCount },
                    icon = { AllVaultsIcon(isItemSharingEnabled = false) },
                    isShared = false,
                    isSelected = homeVaultSelection == VaultSelectionOption.AllVaults,
                    showMenuIcon = false,
                    onClick = { onAllVaultsClick() }
                )
                Divider(
                    modifier = Modifier
                        .padding(horizontal = PassTheme.dimens.bottomsheetHorizontalPadding),
                    color = PassTheme.colors.inputBackgroundStrong
                )
            }
        }

        items(items = list, key = { it.id.id }) { share ->
            VaultDrawerRow(
                name = share.name,
                itemCount = share.activeItemCount,
                icon = {
                    VaultIcon(
                        backgroundColor = share.color.toColor(true),
                        icon = share.icon.toResource(),
                        iconColor = share.color.toColor()
                    )
                },
                isShared = share.isShared,
                isSelected = homeVaultSelection == VaultSelectionOption.Vault(share.id),
                showMenuIcon = true,
                onOptionsClick = { onVaultOptionsClick(share) },
                onClick = { onVaultClick(share.id) }
            )
            Divider(
                modifier = Modifier
                    .padding(horizontal = PassTheme.dimens.bottomsheetHorizontalPadding),
                color = PassTheme.colors.inputBackgroundStrong
            )
        }
        item {
            VaultDrawerRow(
                name = stringResource(R.string.vault_drawer_item_trash),
                itemCount = totalTrashedItems,
                icon = { TrashVaultIcon() },
                isSelected = homeVaultSelection == VaultSelectionOption.Trash,
                isShared = false,
                showMenuIcon = false,
                onOptionsClick = {},
                onClick = onTrashClick
            )
        }
    }
}

