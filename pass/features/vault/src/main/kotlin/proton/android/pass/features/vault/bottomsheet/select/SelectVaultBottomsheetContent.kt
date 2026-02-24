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

package proton.android.pass.features.vault.bottomsheet.select

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemIcon
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemRow
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemSubtitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.container.InfoBanner
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.folders.ExpandCollapseIcon
import proton.android.pass.composecomponents.impl.folders.FolderTree
import proton.android.pass.composecomponents.impl.folders.containsFolderId
import proton.android.pass.composecomponents.impl.folders.expandAncestors
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.vault.R
import java.util.Date
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
fun SelectVaultBottomsheetContent(
    modifier: Modifier = Modifier,
    state: SelectVaultUiState.Success,
    onVaultClick: (ShareId) -> Unit,
    onFolderClick: (ShareId, FolderId) -> Unit,
    onUpgrade: () -> Unit
) {
    Column(
        modifier = modifier
            .bottomSheet()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
    ) {
        if (state.showUpgradeMessage) {
            InfoBanner(
                modifier = Modifier.padding(Spacing.medium, Spacing.none),
                backgroundColor = PassTheme.colors.interactionNormMinor1,
                text = buildAnnotatedString {
                    append(stringResource(R.string.bottomsheet_cannot_select_not_primary_vault))
                    append(' ')
                    withStyle(
                        style = SpanStyle(
                            textDecoration = TextDecoration.Underline,
                            color = PassTheme.colors.loginInteractionNormMajor2
                        )
                    ) {
                        append(stringResource(CompR.string.action_upgrade_now))
                    }
                },
                onClick = onUpgrade
            )
        } else {
            BottomSheetTitle(title = stringResource(R.string.vault_title))
        }

        Column {
            state.vaults.forEachIndexed { index, vaultWithStatus ->
                val shareId = vaultWithStatus.vaultWithItemCount.vault.shareId
                val isVaultSelected = shareId == state.selected.vault.shareId &&
                    state.selectedFolderId == null
                val (subtitle, enabled) = when (vaultWithStatus.status) {
                    is VaultStatus.Disabled -> when (vaultWithStatus.status.reason) {
                        VaultStatus.Reason.ReadOnly ->
                            stringResource(R.string.bottomsheet_select_vault_read_only) to false

                        VaultStatus.Reason.Downgraded ->
                            stringResource(R.string.bottomsheet_select_vault_only_oldest_vaults) to false
                    }

                    VaultStatus.Selectable -> null to true
                }

                val folders = if (state.foldersEnabled) {
                    state.vaultFolders[shareId] ?: emptyList()
                } else {
                    emptyList()
                }

                val hasSelectedFolder = state.foldersEnabled &&
                    state.selectedFolderId != null &&
                    folders.isNotEmpty() &&
                    containsFolderId(folders, state.selectedFolderId)

                val showFoldersState = rememberSaveable(shareId.id) {
                    mutableStateOf(false)
                }

                val expandedState = rememberSaveable(
                    shareId.id,
                    saver = mapSaver(
                        save = { it },
                        restore = { map ->
                            val restored = mutableStateMapOf<String, Boolean>()
                            map.forEach { (key, value) ->
                                if (value is Boolean) restored[key] = value
                            }
                            restored
                        }
                    )
                ) { mutableStateMapOf() }

                LaunchedEffect(folders, state.selectedFolderId) {
                    folders.forEach { folder ->
                        if (!expandedState.contains(folder.id.id)) {
                            expandedState[folder.id.id] = false
                        }
                    }
                    if (hasSelectedFolder) {
                        showFoldersState.value = true
                        expandAncestors(folders, state.selectedFolderId, expandedState)
                    }
                }

                VaultRowWithFolders(
                    vault = vaultWithStatus.vaultWithItemCount,
                    isVaultSelected = isVaultSelected,
                    subtitle = subtitle,
                    enabled = enabled,
                    foldersEnabled = state.foldersEnabled,
                    folders = folders,
                    showFolders = showFoldersState.value,
                    onToggleFolders = { showFoldersState.value = !showFoldersState.value },
                    expandedState = expandedState,
                    selectedFolderId = state.selectedFolderId,
                    onVaultClick = if (enabled) {
                        { onVaultClick(shareId) }
                    } else {
                        null
                    },
                    onFolderClick = { folderId -> onFolderClick(shareId, folderId) }
                )

                if (index < state.vaults.lastIndex) {
                    Divider(
                        modifier = Modifier.padding(horizontal = PassTheme.dimens.bottomsheetHorizontalPadding),
                        color = PassTheme.colors.inputBackgroundStrong
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultRowWithFolders(
    modifier: Modifier = Modifier,
    vault: VaultWithItemCount,
    isVaultSelected: Boolean,
    subtitle: String?,
    enabled: Boolean,
    foldersEnabled: Boolean,
    folders: List<FolderUiModel>,
    showFolders: Boolean,
    onToggleFolders: () -> Unit,
    expandedState: MutableMap<String, Boolean>,
    selectedFolderId: FolderId?,
    onVaultClick: (() -> Unit)?,
    onFolderClick: (FolderId) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        BottomSheetItemRow(
            title = {
                val color = if (enabled) PassTheme.colors.textNorm else PassTheme.colors.textHint
                BottomSheetItemTitle(text = vault.vault.name, color = color)
            },
            subtitle = {
                val textColor =
                    if (enabled) PassTheme.colors.textWeak else PassTheme.colors.textHint
                val text = subtitle ?: pluralStringResource(
                    id = CompR.plurals.bottomsheet_select_vault_item_count,
                    count = vault.activeItemCount.toInt(),
                    vault.activeItemCount
                )
                BottomSheetItemSubtitle(text = text, color = textColor)
            },
            leftIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                ) {
                    if (foldersEnabled && folders.isNotEmpty()) {
                        ExpandCollapseIcon(
                            expanded = showFolders,
                            onClick = onToggleFolders
                        )
                    }
                    VaultIcon(
                        backgroundColor = vault.vault.color.toColor(true),
                        iconColor = vault.vault.color.toColor(),
                        icon = vault.vault.icon.toResource()
                    )
                }
            },
            endIcon = if (vault.vault.shared || isVaultSelected) {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
                    ) {
                        if (vault.vault.shared) {
                            BottomSheetItemIcon(
                                iconId = CoreR.drawable.ic_proton_users,
                                tint = PassTheme.colors.textWeak
                            )
                        }
                        if (isVaultSelected) {
                            BottomSheetItemIcon(
                                iconId = CoreR.drawable.ic_proton_checkmark,
                                tint = PassTheme.colors.loginInteractionNormMajor1
                            )
                        }
                    }
                }
            } else null,
            onClick = onVaultClick
        )

        if (foldersEnabled && folders.isNotEmpty()) {
            AnimatedVisibility(visible = showFolders) {
                FolderTree(
                    modifier = Modifier
                        .padding(horizontal = Spacing.medium)
                        .padding(start = Spacing.large),
                    folders = folders,
                    expandedState = expandedState,
                    selectedFolderId = selectedFolderId,
                    onFolderClick = onFolderClick
                )
            }
        }
    }
}


@Preview
@Composable
fun SelectVaultBottomsheetContentPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val selectedVault = VaultWithItemCount(
        vault = Vault(
            userId = UserId(id = ""),
            shareId = ShareId("123"),
            vaultId = VaultId("123"),
            name = "vault 1",
            createTime = Date(),
            shareFlags = ShareFlags(0)
        ),
        activeItemCount = 12,
        trashedItemCount = 0
    )
    PassTheme(isDark = input.first) {
        Surface {
            SelectVaultBottomsheetContent(
                state = SelectVaultUiState.Success(
                    vaults = persistentListOf(
                        VaultWithStatus(
                            vaultWithItemCount = selectedVault,
                            status = VaultStatus.Selectable
                        ),
                        VaultWithStatus(
                            vaultWithItemCount = VaultWithItemCount(
                                vault = Vault(
                                    userId = UserId(id = ""),
                                    shareId = ShareId("other"),
                                    vaultId = VaultId("123"),
                                    name = "vault 2",
                                    color = ShareColor.Color2,
                                    icon = ShareIcon.Icon2,
                                    createTime = Date(),
                                    shareFlags = ShareFlags(0)
                                ),
                                activeItemCount = 1,
                                trashedItemCount = 0
                            ),
                            status = VaultStatus.Disabled(VaultStatus.Reason.ReadOnly)
                        ),
                        VaultWithStatus(
                            vaultWithItemCount = VaultWithItemCount(
                                vault = Vault(
                                    userId = UserId(id = ""),
                                    shareId = ShareId("another"),
                                    vaultId = VaultId("123"),
                                    name = "vault 3",
                                    color = ShareColor.Color3,
                                    icon = ShareIcon.Icon3,
                                    createTime = Date(),
                                    shareFlags = ShareFlags(0)
                                ),
                                activeItemCount = 1,
                                trashedItemCount = 0
                            ),
                            status = VaultStatus.Disabled(VaultStatus.Reason.Downgraded)
                        )
                    ),
                    selected = selectedVault,
                    showUpgradeMessage = input.second,
                    foldersEnabled = false,
                    vaultFolders = persistentMapOf(),
                    selectedFolderId = null
                ),
                onVaultClick = {},
                onFolderClick = { _, _ -> },
                onUpgrade = {}
            )
        }
    }
}
