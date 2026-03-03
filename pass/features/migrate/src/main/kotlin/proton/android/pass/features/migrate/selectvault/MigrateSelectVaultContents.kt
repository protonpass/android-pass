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

package proton.android.pass.features.migrate.selectvault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetVaultRow
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers
import proton.android.pass.composecomponents.impl.folders.ExpandCollapseIcon
import proton.android.pass.composecomponents.impl.folders.FolderTree
import proton.android.pass.composecomponents.impl.folders.expandAncestors
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.migrate.R

@Composable
fun MigrateSelectVaultContents(
    modifier: Modifier = Modifier,
    vaults: ImmutableList<MigrateVaultState>,
    folderIdToExpand: Option<FolderId>,
    onVaultSelected: (ShareId) -> Unit,
    onFolderSelected: ((ShareId, FolderId) -> Unit)? = null
) {
    if (vaults.any { it.folderTree.isNotEmpty() }) {
        LazyColumn(modifier = modifier) {
            items(items = vaults, key = { it.vaultWithItemCount.vault.shareId.id }) { vaultPair ->
                val vaultWithCount = vaultPair.vaultWithItemCount
                val vaultModel = vaultWithCount.vault

                val (showFolders, onShowFolders) = rememberSaveable {
                    mutableStateOf(folderIdToExpand is Some && vaultPair.folderTree.isNotEmpty())
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        start = if (vaultPair.folderTree.isEmpty()) {
                            PassTheme.dimens.bottomsheetHorizontalPadding
                        } else {
                            0.dp
                        },
                        end = PassTheme.dimens.bottomsheetHorizontalPadding
                    )
                ) {
                    AnimatedVisibility(
                        visible = vaultPair.folderTree.isNotEmpty()
                    ) {
                        ExpandCollapseIcon(
                            expanded = showFolders,
                            onClick = {
                                onShowFolders(!showFolders)
                            }
                        )
                    }

                    BottomSheetVaultRow(
                        vault = vaultWithCount,
                        isSelected = false,
                        customSubtitle = when (vaultPair.status) {
                            is VaultStatus.Enabled -> null
                            is VaultStatus.Disabled -> when (vaultPair.status.reason) {
                                VaultStatus.DisabledReason.NoPermission -> stringResource(
                                    R.string.migrate_disabled_vault_reason_no_permission
                                )

                                VaultStatus.DisabledReason.SameVault -> stringResource(
                                    R.string.migrate_disabled_vault_reason_same_vault
                                )
                            }
                        },
                        enabled = vaultPair.status is VaultStatus.Enabled,
                        onVaultClick = { onVaultSelected(vaultModel.shareId) }
                    ).let { item ->
                        BottomSheetItem(
                            item = item,
                            horizontalPadding = 0.dp
                        )
                    }
                }

                val expandedState = rememberSaveable(
                    saver = mapSaver(
                        save = { it },
                        restore = { map ->
                            val restored = mutableStateMapOf<String, Boolean>()
                            map.forEach { (key, value) ->
                                if (value is Boolean) {
                                    restored[key] = value
                                }
                            }
                            restored
                        }
                    )
                ) {
                    mutableStateMapOf()
                }
                LaunchedEffect(vaultPair.folderTree, folderIdToExpand) {
                    vaultPair.folderTree.forEach { folder ->
                        if (!expandedState.contains(folder.id.id)) {
                            expandedState[folder.id.id] = false
                        }
                    }
                    if (folderIdToExpand is Some) {
                        expandAncestors(vaultPair.folderTree, folderIdToExpand.value, expandedState)
                    }
                }

                AnimatedVisibility(
                    visible = vaultPair.folderTree.isNotEmpty() && showFolders
                ) {
                    FolderTree(
                        modifier = Modifier.padding(start = Spacing.large),
                        folders = vaultPair.folderTree,
                        expandedState = expandedState,
                        onFolderClick = { folderId ->
                            onFolderSelected?.invoke(vaultModel.shareId, folderId)
                        },
                        onThreeDotsClick = null,
                        onCreateFolderClick = null,
                        selectedFolderId = folderIdToExpand
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
                BottomSheetVaultRow(
                    vault = vaultWithCount,
                    isSelected = false,
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
