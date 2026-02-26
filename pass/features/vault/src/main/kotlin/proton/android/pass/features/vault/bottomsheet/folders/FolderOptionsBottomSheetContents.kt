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

package proton.android.pass.features.vault.bottomsheet.folders

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.createSubFolder
import proton.android.pass.composecomponents.impl.bottomsheet.delete
import proton.android.pass.composecomponents.impl.bottomsheet.renameFolder
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers

@Composable
internal fun FolderOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    onEvent: (FolderOptionsUserEvent) -> Unit
) {
    buildList {
        createSubFolder {
            onEvent(FolderOptionsUserEvent.OnCreateSubFolder)
        }.also(::add)

        renameFolder {
            onEvent(FolderOptionsUserEvent.OnRenameFolder)
        }.also(::add)

        delete {
            onEvent(FolderOptionsUserEvent.OnDeleteFolder)
        }.also(::add)

    }.let { items ->
        Column(modifier.bottomSheet()) {
            BottomSheetItemList(
                items = if (items.size > 1) {
                    items.withDividers()
                } else {
                    items
                }.toPersistentList()
            )
        }
    }
}

@Preview
@Composable
internal fun VaultOptionsBottomSheetContentsPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            FolderOptionsBottomSheetContents(
                onEvent = {}
            )
        }
    }
}
