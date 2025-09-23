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

package proton.android.pass.features.home.drawer.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.home.R
import proton.android.pass.searchoptions.api.VaultSelectionOption
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun HomeDrawerList(
    modifier: Modifier = Modifier,
    vaultShares: List<VaultWithItemCount>,
    vaultSelectionOption: VaultSelectionOption,
    allItemsCount: Int,
    hasSharedWithMeItems: Boolean,
    sharedWithMeItemsCount: Int,
    hasSharedByMeItems: Boolean,
    sharedByMeItemsCount: Int,
    trashedItemsCount: Int,
    onUiEvent: (HomeDrawerUiEvent) -> Unit
) {
    LazyColumn(
        modifier = modifier
    ) {
        item {
            HomeDrawerRow(
                shareIconRes = CompR.drawable.ic_brand_pass,
                iconColor = PassTheme.colors.interactionNormMajor2,
                iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                name = stringResource(id = R.string.home_drawer_all_items),
                itemsCount = allItemsCount,
                isSelected = vaultSelectionOption is VaultSelectionOption.AllVaults,
                onClick = {
                    onUiEvent(HomeDrawerUiEvent.OnAllVaultsClick)
                }
            )
        }

        item {
            PassDivider(
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
        }

        items(
            items = vaultShares,
            key = { vaultShare -> vaultShare.vault.shareId.id }
        ) { vaultShare ->
            HomeDrawerRow(
                shareIconRes = vaultShare.vault.icon.toResource(),
                iconColor = vaultShare.vault.color.toColor(),
                iconBackgroundColor = vaultShare.vault.color.toColor(isBackground = true),
                name = vaultShare.vault.name,
                itemsCount = vaultShare.activeItemCount.toInt(),
                membersCount = vaultShare.vault.members,
                isSelected = vaultSelectionOption == VaultSelectionOption.Vault(vaultShare.vault.shareId),
                onClick = {
                    HomeDrawerUiEvent.OnVaultClick(
                        shareId = vaultShare.vault.shareId
                    ).also(onUiEvent)
                },
                onShareClick = {
                    if (vaultShare.vault.shared) {
                        HomeDrawerUiEvent.OnManageVaultClick(shareId = vaultShare.vault.shareId)
                    } else {
                        HomeDrawerUiEvent.OnShareVaultClick(shareId = vaultShare.vault.shareId)
                    }.also(onUiEvent)
                },
                onMenuOptionsClick = {
                    HomeDrawerUiEvent.OnVaultOptionsClick(
                        shareId = vaultShare.vault.shareId
                    ).also(onUiEvent)
                }
            )

            PassDivider(
                modifier = Modifier.padding(horizontal = Spacing.medium)
            )
        }

        if (hasSharedWithMeItems) {
            item {
                HomeDrawerRow(
                    shareIconRes = CoreR.drawable.ic_proton_user_arrow_left,
                    iconColor = PassTheme.colors.interactionNormMajor2,
                    iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                    name = stringResource(id = R.string.item_type_filter_items_shared_with_me),
                    itemsCount = sharedWithMeItemsCount,
                    isSelected = vaultSelectionOption is VaultSelectionOption.SharedWithMe,
                    onClick = {
                        onUiEvent(HomeDrawerUiEvent.OnSharedWithMeClick)
                    }
                )
            }

            item {
                PassDivider(
                    modifier = Modifier.padding(horizontal = Spacing.medium)
                )
            }
        }

        if (hasSharedByMeItems) {
            item {
                HomeDrawerRow(
                    shareIconRes = CoreR.drawable.ic_proton_user_arrow_right,
                    iconColor = PassTheme.colors.interactionNormMajor2,
                    iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                    name = stringResource(id = R.string.item_type_filter_items_shared_by_me),
                    itemsCount = sharedByMeItemsCount,
                    isSelected = vaultSelectionOption is VaultSelectionOption.SharedByMe,
                    onClick = {
                        onUiEvent(HomeDrawerUiEvent.OnSharedByMeClick)
                    }
                )
            }

            item {
                PassDivider(
                    modifier = Modifier.padding(horizontal = Spacing.medium)
                )
            }
        }

        item {
            HomeDrawerRow(
                shareIconRes = CoreR.drawable.ic_proton_trash,
                iconColor = PassTheme.colors.textWeak,
                iconBackgroundColor = PassTheme.colors.textDisabled,
                name = stringResource(id = R.string.vault_drawer_item_trash),
                itemsCount = trashedItemsCount,
                isSelected = vaultSelectionOption is VaultSelectionOption.Trash,
                onClick = {
                    onUiEvent(HomeDrawerUiEvent.OnTrashClick)
                }
            )
        }
    }
}
