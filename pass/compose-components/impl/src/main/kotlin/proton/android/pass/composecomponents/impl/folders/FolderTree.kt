/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.composecomponents.impl.folders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.FolderUiModel
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.folders.mock.FoldersParameter
import proton.android.pass.composecomponents.impl.folders.mock.ThemedFoldersPreviewProvider
import proton.android.pass.composecomponents.impl.icon.PassPlusIcon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.FolderId
import me.proton.core.presentation.R as CoreR

private fun MutableMap<String, Boolean>.isExpanded(id: String) = this[id] ?: false

private fun MutableMap<String, Boolean>.toggle(id: String) {
    this[id] = !(this[id] ?: false)
}

@Composable
fun FolderTree(
    modifier: Modifier = Modifier,
    modifierCreateButton: Modifier = Modifier,
    folders: List<FolderUiModel>,
    expandedState: MutableMap<String, Boolean>,
    selectedFolderId: FolderId? = null,
    depth: Int = 0, // no padding when depth == 0
    needsToUpgrade: Boolean = false,
    onFolderClick: ((FolderId) -> Unit)?,
    onThreeDotsClick: ((FolderId) -> Unit)? = null,
    onCreateFolderClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        onCreateFolderClick?.let {
            if (folders.isEmpty() && depth == 0) {
                CreateFolderButton(
                    modifier = modifierCreateButton,
                    needsToUpgrade = needsToUpgrade,
                    onClick = { onCreateFolderClick.invoke() }
                )
            }
        }

        folders.forEach { folder ->

            val isExpanded = expandedState.isExpanded(folder.id.id)

            OneFolderItem(
                folderName = folder.name,
                folders = folder.folders,
                isExpanded = isExpanded,
                isSelected = selectedFolderId != null && folder.id == selectedFolderId,
                onExpandToggle = { expandedState.toggle(folder.id.id) },
                onThreeDotsClick = onThreeDotsClick?.let { { it(folder.id) } },
                onFolderClick = onFolderClick?.let { { it(folder.id) } }
            )

            AnimatedVisibility(visible = folder.folders.isNotEmpty() && isExpanded) {
                FolderTree(
                    modifier = Modifier.padding(start = (depth + 1 * 16).dp),
                    folders = folder.folders,
                    expandedState = expandedState,
                    onThreeDotsClick = onThreeDotsClick,
                    onCreateFolderClick = onCreateFolderClick,
                    selectedFolderId = selectedFolderId,
                    depth = depth + 1,
                    onFolderClick = onFolderClick
                )
            }
        }
    }
}

@Composable
private fun CreateFolderButton(
    modifier: Modifier = Modifier,
    needsToUpgrade: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(shape = CircleShape)
            .background(color = PassTheme.colors.interactionNormMinor1)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(CoreR.drawable.ic_proton_folder_plus),
            contentDescription = null,
            tint = PassTheme.colors.interactionNormMajor2
        )
        Text.Body2Regular(
            text = stringResource(R.string.folder_tree_create_folder),
            color = PassTheme.colors.interactionNormMajor2
        )
        if (needsToUpgrade) {
            PassPlusIcon()
        }
    }
}

@[Preview Composable]
internal fun FolderTreePreview(
    @PreviewParameter(ThemedFoldersPreviewProvider::class) input: Pair<Boolean, FoldersParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            FolderTree(
                folders = input.second.folders,
                expandedState = remember {
                    mutableStateMapOf<String, Boolean>()
                        .apply { putAll(input.second.expandedState) }
                },
                selectedFolderId = null,
                needsToUpgrade = input.second.needsToUpgrade,
                onThreeDotsClick = {},
                onCreateFolderClick = {},
                onFolderClick = {}
            )
        }
    }
}
