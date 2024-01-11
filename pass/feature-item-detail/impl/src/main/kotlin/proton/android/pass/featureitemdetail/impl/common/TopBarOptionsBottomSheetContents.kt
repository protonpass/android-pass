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

package proton.android.pass.featureitemdetail.impl.common

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItem
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemList
import proton.android.pass.composecomponents.impl.bottomsheet.withDividers

@Composable
fun TopBarOptionsBottomSheetContents(
    modifier: Modifier = Modifier,
    canMigrate: Boolean,
    canMoveToTrash: Boolean,
    onMigrate: () -> Unit,
    onMoveToTrash: () -> Unit,
    isPinned: Boolean,
    onPinned: () -> Unit,
    onUnpinned: () -> Unit,
    isPinningFeatureEnabled: Boolean,
) {
    val items = mutableListOf<BottomSheetItem>().apply {
        if (canMigrate) {
            add(migrate(onClick = onMigrate))
        }

        if (isPinningFeatureEnabled) {
            if (isPinned) {
                add(unpin(onClick = onUnpinned))
            } else {
                add(pin(onClick = onPinned))
            }
        }

        if (canMoveToTrash) {
            add(moveToTrash(onClick = onMoveToTrash))
        }
    }

    BottomSheetItemList(
        modifier = modifier.bottomSheet(),
        items = items.withDividers().toPersistentList()
    )
}

class ThemedTopBarOptionsPreviewProvider :
    ThemePairPreviewProvider<TopBarOptionsParameters>(TopBarOptionsParametersPreviewProvider())

@Preview
@Composable
fun TopBarOptionsBottomSheetContentsPreview(
    @PreviewParameter(ThemedTopBarOptionsPreviewProvider::class) input: Pair<Boolean, TopBarOptionsParameters>
) {
    val (isDark, params) = input

    PassTheme(isDark = isDark) {
        Surface {
            with(params) {
                TopBarOptionsBottomSheetContents(
                    canMigrate = canMigrate,
                    canMoveToTrash = canMoveToTrash,
                    isPinned = isPinned,
                    onMigrate = {},
                    onMoveToTrash = {},
                    onPinned = {},
                    onUnpinned = {},
                    isPinningFeatureEnabled = true,
                )
            }
        }
    }
}
