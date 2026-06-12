/*
 * Copyright (c) 2024-2026 Proton AG
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.folders.NamespacedExpandedState
import proton.android.pass.composecomponents.impl.folders.folderTreeItems
import proton.android.pass.composecomponents.impl.folders.allFolderIds
import proton.android.pass.composecomponents.impl.folders.containsFolderId
import proton.android.pass.composecomponents.impl.folders.expandAncestors
import proton.android.pass.composecomponents.impl.folders.foldersToExpand
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.home.R
import proton.android.pass.searchoptions.api.VaultSelectionOption
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

private fun booleanMapSaver() = mapSaver<MutableMap<String, Boolean>>(
    save = { it },
    restore = { map ->
        val restored = mutableStateMapOf<String, Boolean>()
        map.forEach { (key, value) -> if (value is Boolean) restored[key] = value }
        restored
    }
)

@Composable
internal fun HomeDrawerList(
    modifier: Modifier = Modifier,
    vaultShares: List<VaultWithItemCount>,
    vaultFolders: Map<ShareId, List<FolderUiModel>>,
    vaultFolderAtLimit: Set<ShareId> = emptySet(),
    vaultSelectionOption: VaultSelectionOption,
    allItemsCount: Int,
    foldersEnabled: Boolean,
    canCreateFolderShareIds: Set<ShareId>,
    canCreateFolderNeedsUpgradeShareIds: Set<ShareId>,
    hasSharedWithMeItems: Boolean,
    sharedWithMeItemsCount: Int,
    hasSharedByMeItems: Boolean,
    sharedByMeItemsCount: Int,
    trashedItemsCount: Int,
    onUiEvent: (HomeDrawerUiEvent) -> Unit
) {
    val vaultShowFoldersMap = rememberSaveable(saver = booleanMapSaver()) { mutableStateMapOf() }
    val folderExpandedMap = rememberSaveable(saver = booleanMapSaver()) { mutableStateMapOf() }
    val knownFolderIdsMap = remember { mutableMapOf<String, Set<String>>() }

    LazyColumn(modifier = modifier) {
        item {
            HomeDrawerRow(
                shareIconRes = CompR.drawable.ic_brand_pass,
                iconColor = PassTheme.colors.interactionNormMajor2,
                iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                name = stringResource(id = R.string.home_drawer_all_items),
                itemsCount = allItemsCount,
                isSelected = vaultSelectionOption is VaultSelectionOption.AllVaults,
                onClick = { onUiEvent(HomeDrawerUiEvent.OnAllVaultsClick) }
            )
        }

        item {
            PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
        }

        vaultShares.forEach { vaultShare ->
            val shareId = vaultShare.vault.shareId
            val shareIdStr = shareId.id
            val selectedFolderIdForVault: FolderId? =
                (vaultSelectionOption as? VaultSelectionOption.Folder)
                    ?.takeIf { it.shareId == shareId }
                    ?.folderId
            val folders = vaultFolders[shareId] ?: emptyList()
            val effectiveCanCreate = canCreateFolderShareIds.contains(shareId) && !vaultFolderAtLimit.contains(shareId)
            val vaultNeedsUpgrade = canCreateFolderNeedsUpgradeShareIds.contains(shareId)
            val shouldShowFolderContent = foldersEnabled &&
                (folders.isNotEmpty() || effectiveCanCreate || vaultNeedsUpgrade)
            val isShowingFolders = vaultShowFoldersMap[shareIdStr] ?: false
            val vaultFolderExpandedMap = NamespacedExpandedState(folderExpandedMap, shareIdStr)

            item(key = shareIdStr) {
                LaunchedEffect(folders) {
                    val currentIds = allFolderIds(folders)
                    val toExpand = foldersToExpand(knownFolderIdsMap[shareIdStr], currentIds, folders)
                    if (toExpand.isNotEmpty()) {
                        vaultShowFoldersMap[shareIdStr] = true
                        toExpand.forEach { id -> vaultFolderExpandedMap[id] = true }
                    }
                    knownFolderIdsMap[shareIdStr] = currentIds
                    folders.forEach { folder ->
                        if (!vaultFolderExpandedMap.contains(folder.id.id)) {
                            vaultFolderExpandedMap[folder.id.id] = false
                        }
                    }
                }

                LaunchedEffect(selectedFolderIdForVault) {
                    if (selectedFolderIdForVault != null &&
                        containsFolderId(folders, selectedFolderIdForVault)
                    ) {
                        vaultShowFoldersMap[shareIdStr] = true
                        expandAncestors(folders, selectedFolderIdForVault, vaultFolderExpandedMap)
                    }
                }

                HomeDrawerRow(
                    modifier = Modifier.animateItem(),
                    shareIconRes = vaultShare.vault.icon.toResource(),
                    iconColor = vaultShare.vault.color.toColor(),
                    iconBackgroundColor = vaultShare.vault.color.toColor(isBackground = true),
                    name = vaultShare.vault.name,
                    itemsCount = vaultShare.activeItemCount.toInt(),
                    membersCount = vaultShare.vault.members,
                    isSelected = vaultSelectionOption == VaultSelectionOption.Vault(shareId) ||
                        vaultSelectionOption is VaultSelectionOption.Folder &&
                        vaultSelectionOption.shareId == shareId,
                    onClick = {
                        HomeDrawerUiEvent.OnVaultClick(shareId = vaultShare.vault.shareId)
                            .also(onUiEvent)
                    },
                    onShareClick = {
                        if (vaultShare.vault.shared) {
                            HomeDrawerUiEvent.OnManageVaultClick(shareId = vaultShare.vault.shareId)
                        } else {
                            HomeDrawerUiEvent.OnShareVaultClick(shareId = vaultShare.vault.shareId)
                        }.also(onUiEvent)
                    },
                    onMenuOptionsClick = {
                        HomeDrawerUiEvent.OnVaultOptionsClick(shareId = vaultShare.vault.shareId)
                            .also(onUiEvent)
                    },
                    foldersEnabled = foldersEnabled,
                    hasFolderContent = shouldShowFolderContent,
                    showFolders = isShowingFolders && shouldShowFolderContent,
                    onShowFoldersToggle = if (shouldShowFolderContent) {
                        { vaultShowFoldersMap[shareIdStr] = !isShowingFolders }
                    } else null
                )
            }

            if (shouldShowFolderContent && isShowingFolders) {
                folderTreeItems(
                    folders = folders,
                    expandedState = vaultFolderExpandedMap,
                    selectedFolderId = selectedFolderIdForVault.toOption(),
                    startPadding = Spacing.large,
                    canCreateFolder = effectiveCanCreate || vaultNeedsUpgrade,
                    needsToUpgrade = vaultNeedsUpgrade,
                    keyPrefix = "${shareIdStr}_",
                    createButtonModifier = Modifier
                        .padding(start = 20.dp)
                        .padding(bottom = Spacing.medium),
                    onThreeDotsClick = if (
                        canCreateFolderShareIds.contains(shareId) ||
                        canCreateFolderNeedsUpgradeShareIds.contains(shareId)
                    ) {
                        { HomeDrawerUiEvent.OnFolderOptionsClick(shareId, it).also(onUiEvent) }
                    } else null,
                    onFolderClick = {
                        HomeDrawerUiEvent.OnFolderClick(shareId, it).also(onUiEvent)
                    },
                    onCreateFolderClick = if (effectiveCanCreate || vaultNeedsUpgrade) {
                        {
                            if (vaultNeedsUpgrade) onUiEvent(HomeDrawerUiEvent.OnUpgradeClick)
                            else HomeDrawerUiEvent.OnCreateFolderClick(shareId).also(onUiEvent)
                        }
                    } else null
                )
            }

            item(key = "divider_$shareIdStr") {
                PassDivider(
                    modifier = Modifier
                        .animateItem()
                        .padding(horizontal = Spacing.medium)
                )
            }
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
                    onClick = { onUiEvent(HomeDrawerUiEvent.OnSharedWithMeClick) }
                )
            }

            item {
                PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
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
                    onClick = { onUiEvent(HomeDrawerUiEvent.OnSharedByMeClick) }
                )
            }

            item {
                PassDivider(modifier = Modifier.padding(horizontal = Spacing.medium))
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
                onClick = { onUiEvent(HomeDrawerUiEvent.OnTrashClick) }
            )
        }
    }
}
