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

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.composecomponents.impl.badge.CircledBadge
import proton.android.pass.composecomponents.impl.badge.OverlayBadge
import proton.android.pass.composecomponents.impl.buttons.PassSharingShareIcon
import proton.android.pass.composecomponents.impl.folders.ExpandCollapseIcon
import proton.android.pass.composecomponents.impl.folders.FolderTree
import proton.android.pass.composecomponents.impl.folders.containsFolderId
import proton.android.pass.composecomponents.impl.folders.expandAncestors
import proton.android.pass.composecomponents.impl.folders.mock.FoldersParameter
import proton.android.pass.composecomponents.impl.folders.mock.ThemedFoldersPreviewProvider
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.home.R
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun HomeDrawerRow(
    modifier: Modifier = Modifier,
    @DrawableRes shareIconRes: Int,
    iconColor: Color,
    iconBackgroundColor: Color,
    name: String,
    itemsCount: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    membersCount: Int = 0,
    foldersEnabled: Boolean = false,
    selectedFolderId: FolderId? = null,
    folders: List<FolderUiModel> = emptyList(),
    showFoldersInitially: Boolean = false,
    onFolderClick: ((FolderId) -> Unit)? = null,
    onMenuOptionsClickFromFolder: ((FolderId) -> Unit)? = null,
    onCreateFolderClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onMenuOptionsClick: (() -> Unit)? = null,
    needsToUpgrade: Boolean = false
) {
    val (showFolders, onShowFolders) = rememberSaveable { mutableStateOf(showFoldersInitially) }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(
                    start = Spacing.medium,
                    top = Spacing.mediumSmall,
                    bottom = Spacing.mediumSmall
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(
                visible = foldersEnabled
            ) {
                ExpandCollapseIcon(
                    expanded = showFolders,
                    onClick = {
                        onShowFolders(!showFolders)
                    }
                )
            }

            OverlayBadge(
                isShown = isSelected,
                content = {
                    VaultIcon(
                        icon = shareIconRes,
                        iconColor = iconColor,
                        backgroundColor = iconBackgroundColor
                    )
                },
                badge = {
                    CircledBadge(
                        ratio = 0.75F,
                        icon = CoreR.drawable.ic_proton_checkmark,
                        iconColor = PassTheme.colors.interactionNormMinor1,
                        backgroundColor = PassTheme.colors.interactionNormMajor2
                    )
                }
            )

            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .padding(start = Spacing.mediumSmall),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
            ) {
                Text.Body1Regular(
                    text = name
                )

                Text.Body2Regular(
                    text = pluralStringResource(
                        R.plurals.vault_drawer_vaults_item_count,
                        itemsCount,
                        itemsCount
                    ),
                    color = PassTheme.colors.textWeak
                )
            }

            onShareClick?.let { onClick ->
                PassSharingShareIcon(
                    itemCategory = ItemCategory.Unknown,
                    shareSharedCount = membersCount,
                    onClick = onClick
                )
            }

            onMenuOptionsClick?.let { onClick ->
                ThreeDotsMenuButton(
                    onClick = onClick
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

        LaunchedEffect(folders, selectedFolderId) {
            folders.forEach { folder ->
                if (!expandedState.contains(folder.id.id)) {
                    expandedState[folder.id.id] = showFoldersInitially
                }
            }
            val shouldExpand = foldersEnabled &&
                selectedFolderId != null &&
                folders.isNotEmpty() &&
                containsFolderId(folders, selectedFolderId)
            if (shouldExpand) {
                onShowFolders(true)
                expandAncestors(folders, selectedFolderId, expandedState)
            }
        }

        AnimatedVisibility(
            visible = foldersEnabled && showFolders
        ) {
            FolderTree(
                modifier = Modifier.padding(start = Spacing.large),
                folders = folders,
                expandedState = expandedState,
                selectedFolderId = selectedFolderId,
                onThreeDotsClick = {
                    onMenuOptionsClickFromFolder?.invoke(it)
                },
                onCreateFolderClick = {
                    onCreateFolderClick?.invoke()
                },
                modifierCreateButton = Modifier
                    .padding(start = 20.dp)
                    .padding(bottom = Spacing.medium),
                onFolderClick = {
                    onFolderClick?.invoke(it)
                },
                needsToUpgrade = needsToUpgrade
            )
        }
    }
}

@[Preview Composable]
internal fun HomeDrawerRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isSelected) = input

    PassTheme(isDark = isDark) {
        Surface {
            HomeDrawerRow(
                shareIconRes = CompR.drawable.ic_brand_pass,
                iconColor = PassTheme.colors.interactionNormMajor2,
                iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                name = "Share name",
                itemsCount = 16,
                membersCount = 5,
                isSelected = isSelected,
                onClick = {},
                onShareClick = {},
                onMenuOptionsClick = {},
                foldersEnabled = false,
                folders = emptyList()
            )
        }
    }
}

@[Preview Composable]
internal fun HomeDrawerRowEmptyFoldersPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDark, isSelected) = input

    PassTheme(isDark = isDark) {
        Surface {
            HomeDrawerRow(
                shareIconRes = CompR.drawable.ic_brand_pass,
                iconColor = PassTheme.colors.interactionNormMajor2,
                iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                name = "Share name",
                itemsCount = 16,
                membersCount = 5,
                isSelected = isSelected,
                showFoldersInitially = true,
                onClick = {},
                onShareClick = {},
                onMenuOptionsClick = {},
                foldersEnabled = true,
                folders = emptyList()
            )
        }
    }
}

@[Preview Composable]
internal fun HomeDrawerRowWithFoldersPreview(
    @PreviewParameter(ThemedFoldersPreviewProvider::class) input: Pair<Boolean, FoldersParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            HomeDrawerRow(
                shareIconRes = CompR.drawable.ic_brand_pass,
                iconColor = PassTheme.colors.interactionNormMajor2,
                iconBackgroundColor = PassTheme.colors.interactionNormMinor1,
                name = "Share name",
                itemsCount = 16,
                membersCount = 5,
                isSelected = false,
                showFoldersInitially = true,
                onClick = {},
                onShareClick = {},
                onMenuOptionsClick = {},
                foldersEnabled = true,
                folders = input.second.folders
            )
        }
    }
}
