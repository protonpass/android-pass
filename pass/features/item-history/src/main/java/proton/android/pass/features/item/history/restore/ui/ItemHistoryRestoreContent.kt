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

package proton.android.pass.features.item.history.restore.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsContent
import proton.android.pass.composecomponents.impl.utils.protonItemColors
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import proton.android.pass.features.item.history.restore.presentation.ItemHistoryRestoreState

@Composable
internal fun ItemHistoryRestoreContent(
    modifier: Modifier = Modifier,
    onNavigated: (ItemHistoryNavDestination) -> Unit,
    state: ItemHistoryRestoreState,
) {
    val itemUiModel = state.itemUiModel ?: return

    val itemColors = protonItemColors(itemCategory = itemUiModel.category)

    PassItemDetailsContent(
        modifier = modifier,
        itemColors = itemColors,
        itemUiModel = itemUiModel,
        topBar = {
            ItemHistoryRestoreTopBar(
                colors = itemColors,
                onUpClick = { onNavigated(ItemHistoryNavDestination.Back) },
                onRestoreClick = {
                    // this will be implemented in the following MR
                },
            )
        }
    )
}
