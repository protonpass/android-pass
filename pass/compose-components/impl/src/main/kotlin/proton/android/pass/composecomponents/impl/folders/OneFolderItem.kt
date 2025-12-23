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

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.FolderWithItemCount
import proton.android.pass.composecomponents.impl.folders.mock.mockFolders
import me.proton.core.presentation.R as CoreR


@Composable
fun OneFolderItem(
    modifier: Modifier = Modifier,
    folderName: String,
    folders: List<FolderWithItemCount> = emptyList(),
    isExpanded: Boolean,
    onFolderClick: () -> Unit,
    onExpandToggle: () -> Unit,
    onThreeDotsClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedContent(
            targetState = folders.isNotEmpty()
        ) { hasFolders ->
            if (hasFolders) {
                ExpandCollapseIcon(
                    expanded = isExpanded,
                    onClick = {
                        onExpandToggle()
                    }
                )
            } else {
                Box(modifier = Modifier.size(32.dp)) { }
            }
        }

        Row(
            modifier = Modifier.clickable {
                onFolderClick()
            },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon.Default(
                modifier = Modifier.size(20.dp),
                tint = Color(color = 0xFFE9A944),
                id = if (folders.isNotEmpty())
                    CoreR.drawable.ic_proton_folders
                else
                    CoreR.drawable.ic_proton_folder
            )

            Text.Body1Regular(
                modifier = Modifier.weight(1f),
                text = folderName,
                color = ProtonTheme.colors.textNorm,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            onThreeDotsClick?.let {
                ThreeDotsMenuButton(
                    onClick = it
                )
            }
        }
    }
}

@[Preview Composable]
internal fun HomeDrawerFolderRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isExpanded, onExpandToggle) = remember { mutableStateOf(false) }
    PassTheme(isDark = input.first) {
        Surface {
            OneFolderItem(
                folders = mockFolders,
                folderName = "a folder",
                isExpanded = isExpanded,
                onExpandToggle = {
                    onExpandToggle(!isExpanded)
                },
                onThreeDotsClick = {},
                onFolderClick = {}
            )
        }
    }
}

