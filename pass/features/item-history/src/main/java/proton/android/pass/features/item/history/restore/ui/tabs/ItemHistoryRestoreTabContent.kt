/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.item.history.restore.ui.tabs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.features.item.history.restore.ItemHistoryRestoreUiEvent
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreSelection

private const val ITEM_HISTORY_TAB_INDEX_REVISION = 0
private const val ITEM_HISTORY_TAB_INDEX_CURRENT = 1

@Composable
internal fun ItemHistoryRestoreTabContent(
    modifier: Modifier = Modifier,
    revisionItemDetailState: ItemDetailState,
    currentItemDetailState: ItemDetailState,
    itemColors: PassItemColors,
    onEvent: (ItemHistoryRestoreUiEvent) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(ITEM_HISTORY_TAB_INDEX_REVISION) }

    Box(modifier = modifier.fillMaxSize()) {
        ItemHistoryRestoreTab(
            modifier = Modifier.applyIf(
                condition = selectedTabIndex == ITEM_HISTORY_TAB_INDEX_REVISION,
                ifTrue = { zIndex(1f) },
                ifFalse = { zIndex(0f) }
            ),
            itemDetailState = revisionItemDetailState,
            itemColors = itemColors,
            onEvent = onEvent,
            selection = ItemHistoryRestoreSelection.Revision
        )

        ItemHistoryRestoreTab(
            modifier = Modifier.applyIf(
                condition = selectedTabIndex == ITEM_HISTORY_TAB_INDEX_CURRENT,
                ifTrue = { zIndex(1f) },
                ifFalse = { zIndex(0f) }
            ),
            itemDetailState = currentItemDetailState,
            itemColors = itemColors,
            onEvent = onEvent,
            selection = ItemHistoryRestoreSelection.Current
        )

        ItemHistoryRestoreTabRow(
            modifier = Modifier
                .zIndex(2f)
                .padding(
                    start = Spacing.medium,
                    end = Spacing.medium,
                    bottom = Spacing.small
                )
                .align(Alignment.BottomCenter),
            selectedTabIndex = selectedTabIndex,
            itemColors = itemColors,
            onSelectTab = { newSelectedTabIndex ->
                selectedTabIndex = newSelectedTabIndex
            }
        )
    }
}
