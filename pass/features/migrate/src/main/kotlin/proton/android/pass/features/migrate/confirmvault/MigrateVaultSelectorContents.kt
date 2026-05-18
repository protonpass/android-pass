/*
 * Copyright (c) 2026 Proton AG
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.folders.ExpandCollapseIcon
import proton.android.pass.composecomponents.impl.folders.NamespacedExpandedState
import proton.android.pass.composecomponents.impl.folders.expandAncestors
import proton.android.pass.composecomponents.impl.folders.folderTreeItems
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.migrate.R

@Composable
internal fun MigrateVaultSelectorContents(
    modifier: Modifier = Modifier,
    vaults: ImmutableList<MigrateVaultState>,
    folderIdToExpand: Option<FolderId>,
    selectedShareId: Option<ShareId> = None,
    selectedFolderId: Option<FolderId> = None,
    disabledFolderId: Option<FolderId> = None,
    disabledFolderItemCount: Int = 0,
    disabledFolderReasonOverride: String? = null,
    disabledDescendantFolderIds: Set<FolderId> = emptySet(),
    disabledDescendantFolderReason: String? = null,
    movingFolderId: Option<FolderId> = None,
    movingFolderReason: String? = null,
    startWithVaultExpanded: Boolean = false,
    onVaultSelected: (ShareId) -> Unit,
    onFolderSelected: ((ShareId, FolderId) -> Unit)? = null
) {
    val disabledCount = disabledFolderItemCount.coerceAtLeast(1)
    val primaryDisabledReason = if (disabledFolderId is Some) {
        disabledFolderReasonOverride ?: pluralStringResource(
            id = R.plurals.migrate_disabled_folder_reason_same_folder,
            count = disabledCount,
            disabledCount
        )
    } else {
        null
    }
    val disabledFolders = remember(
        disabledFolderId, primaryDisabledReason,
        movingFolderId, movingFolderReason,
        disabledDescendantFolderIds, disabledDescendantFolderReason
    ) {
        buildMap {
            if (disabledFolderId is Some && primaryDisabledReason != null) {
                put(disabledFolderId.value, primaryDisabledReason)
            }
            if (movingFolderId is Some && movingFolderReason != null) {
                put(movingFolderId.value, movingFolderReason)
            }
            if (disabledDescendantFolderReason != null) {
                disabledDescendantFolderIds.forEach { put(it, disabledDescendantFolderReason) }
            }
        }
    }
    if (vaults.any { it.hasFolders }) {
        val vaultShowFoldersMap = remember { mutableStateMapOf<String, Boolean>() }
        val folderExpandedMap = remember { mutableStateMapOf<String, Boolean>() }

        LazyColumn(modifier = modifier) {
            vaults.forEach { vaultPair ->
                val vaultWithCount = vaultPair.vaultWithItemCount
                val vaultModel = vaultWithCount.vault
                val shareIdStr = vaultModel.shareId.id
                val hasFolderToExpand = folderIdToExpand is Some
                val showFolders = vaultShowFoldersMap[shareIdStr] ?: (hasFolderToExpand || startWithVaultExpanded)
                val vaultFolderExpandedMap = NamespacedExpandedState(folderExpandedMap, shareIdStr)
                val isVaultSelected = selectedShareId is Some &&
                    selectedShareId.value == vaultModel.shareId &&
                    selectedFolderId is None

                item(key = shareIdStr) {
                    LaunchedEffect(Unit) {
                        if (!vaultShowFoldersMap.contains(shareIdStr)) {
                            vaultShowFoldersMap[shareIdStr] = folderIdToExpand is Some || startWithVaultExpanded
                        }
                        vaultPair.folderTree.forEach { folder ->
                            if (!vaultFolderExpandedMap.contains(folder.id.id)) {
                                vaultFolderExpandedMap[folder.id.id] = false
                            }
                        }
                    }

                    LaunchedEffect(folderIdToExpand) {
                        if (folderIdToExpand is Some) {
                            expandAncestors(
                                vaultPair.folderTree,
                                folderIdToExpand.value,
                                vaultFolderExpandedMap
                            )
                        }
                    }

                    LaunchedEffect(vaultPair.folderTree) {
                        if (folderIdToExpand is Some) {
                            fun initExpanded(folders: List<FolderUiModel>) {
                                folders.forEach { folder ->
                                    if (!vaultFolderExpandedMap.contains(folder.id.id)) {
                                        vaultFolderExpandedMap[folder.id.id] = true
                                    }
                                    initExpanded(folder.folders)
                                }
                            }
                            initExpanded(vaultPair.folderTree)
                        }
                    }

                    val customSubtitle = when (vaultPair.status) {
                        is VaultStatus.Enabled -> null
                        is VaultStatus.Disabled -> when (vaultPair.status.reason) {
                            VaultStatus.DisabledReason.NoPermission -> stringResource(
                                R.string.migrate_disabled_vault_reason_no_permission
                            )
                            VaultStatus.DisabledReason.SameVault -> stringResource(
                                R.string.migrate_disabled_vault_reason_same_vault
                            )
                        }
                    }
                    val isEnabled = vaultPair.status is VaultStatus.Enabled

                    if (vaultPair.hasFolders) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isEnabled) { onVaultSelected(vaultModel.shareId) }
                                .padding(end = PassTheme.dimens.bottomsheetHorizontalPadding)
                        ) {
                            ExpandCollapseIcon(
                                expanded = showFolders,
                                onClick = { vaultShowFoldersMap[shareIdStr] = !showFolders }
                            )
                            BottomSheetVaultRow(
                                vault = vaultWithCount,
                                isSelected = isVaultSelected,
                                customSubtitle = customSubtitle,
                                enabled = isEnabled,
                                onVaultClick = null
                            ).let { item ->
                                BottomSheetItem(item = item, horizontalPadding = 0.dp)
                            }
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = isEnabled) { onVaultSelected(vaultModel.shareId) }
                                .padding(horizontal = PassTheme.dimens.bottomsheetHorizontalPadding)
                        ) {
                            BottomSheetVaultRow(
                                vault = vaultWithCount,
                                isSelected = isVaultSelected,
                                customSubtitle = customSubtitle,
                                enabled = isEnabled,
                                onVaultClick = null
                            ).let { item ->
                                BottomSheetItem(item = item, horizontalPadding = 0.dp)
                            }
                        }
                    }
                }

                if (vaultPair.hasFolders && showFolders) {
                    folderTreeItems(
                        folders = vaultPair.folderTree,
                        expandedState = vaultFolderExpandedMap,
                        selectedFolderId = selectedFolderId,
                        startPadding = Spacing.large,
                        keyPrefix = shareIdStr,
                        onFolderClick = { folderId ->
                            onFolderSelected?.invoke(vaultModel.shareId, folderId)
                        },
                        disabledFolders = disabledFolders
                    )
                }
            }
        }
    } else {
        BottomSheetItemList(
            modifier = modifier,
            items = vaults.map { vault ->
                val vaultWithCount = vault.vaultWithItemCount
                val vaultModel = vaultWithCount.vault
                val isVaultSelected = selectedShareId is Some &&
                    selectedShareId.value == vaultModel.shareId &&
                    selectedFolderId is None
                BottomSheetVaultRow(
                    vault = vaultWithCount,
                    isSelected = isVaultSelected,
                    customSubtitle = when (vault.status) {
                        is VaultStatus.Enabled -> null
                        is VaultStatus.Disabled -> when (vault.status.reason) {
                            VaultStatus.DisabledReason.NoPermission -> stringResource(
                                R.string.migrate_disabled_vault_reason_no_permission
                            )
                            VaultStatus.DisabledReason.SameVault -> stringResource(
                                R.string.migrate_disabled_vault_reason_same_vault
                            )
                        }
                    },
                    enabled = vault.status is VaultStatus.Enabled,
                    onVaultClick = { onVaultSelected(vaultModel.shareId) }
                )
            }
                .withDividers()
                .toImmutableList()
        )
    }
}
