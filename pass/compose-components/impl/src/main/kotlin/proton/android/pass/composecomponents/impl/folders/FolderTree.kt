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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.FolderWithItemCount
import proton.android.pass.composecomponents.impl.folders.mock.mockFolders
import kotlin.collections.forEach

private fun MutableMap<String, Boolean>.isExpanded(id: String) = this[id] ?: false

private fun MutableMap<String, Boolean>.toggle(id: String) {
    this[id] = !(this[id] ?: false)
}

@Composable
fun FolderTree(
    modifier: Modifier = Modifier,
    folders: List<FolderWithItemCount>,
    onFolderClick: (FolderId) -> Unit,
    onThreeDotsClick: ((FolderId) -> Unit)? = null,
    expandedState: MutableMap<String, Boolean>,
    depth: Int = 0 // no padding when depth == 0
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        folders.forEach { folder ->

            val isExpanded = expandedState.isExpanded(folder.id.id)

            OneFolderItem(
                folderName = folder.name,
                folders = folder.folders,
                isExpanded = isExpanded,
                onExpandToggle = {
                    expandedState.toggle(folder.id.id)
                },
                onThreeDotsClick = onThreeDotsClick?.let {
                    { it(folder.id) }
                },
                onFolderClick = {
                    onFolderClick(folder.id)
                }
            )

            AnimatedVisibility(visible = folder.folders.isNotEmpty() && isExpanded) {
                FolderTree(
                    modifier = Modifier.padding(start = (depth + 1 * 16).dp),
                    folders = folder.folders,
                    expandedState = expandedState,
                    onThreeDotsClick = onThreeDotsClick,
                    depth = depth + 1,
                    onFolderClick = onFolderClick
                )
            }
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
            FolderTree(
                folders = mockFolders,
                expandedState = remember { mutableStateMapOf() },
                onThreeDotsClick = {},
                onFolderClick = {}
            )
        }
    }
}
