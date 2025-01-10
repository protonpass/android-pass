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

package proton.android.pass.features.item.history.timeline.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.features.item.history.R
import proton.android.pass.features.item.history.navigation.ItemHistoryNavDestination
import proton.android.pass.features.item.history.timeline.presentation.ItemHistoryTimelineState

@Composable
internal fun ItemHistoryTimelineContent(
    modifier: Modifier = Modifier,
    state: ItemHistoryTimelineState,
    onNavigated: (ItemHistoryNavDestination) -> Unit
) = with(state) {

    val itemColors = passItemColors(itemCategory = itemCategory)

    Scaffold(
        modifier = modifier,
        topBar = {
            ItemHistoryTimelineTopBar(
                colors = itemColors,
                onUpClick = { onNavigated(ItemHistoryNavDestination.Back) },
                onOptions = {
                    if (this@with is ItemHistoryTimelineState.Success) {
                        onNavigated(ItemHistoryNavDestination.Options(shareId, itemId))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding)
        ) {
            ItemHistoryTimelineTitle(
                modifier = Modifier.padding(
                    horizontal = Spacing.medium,
                    vertical = Spacing.large
                ),
                text = stringResource(id = R.string.item_history_timeline_title)
            )

            when (this@with) {
                ItemHistoryTimelineState.Error -> {
                    onNavigated(ItemHistoryNavDestination.Back)
                }

                ItemHistoryTimelineState.Loading -> {
                    ItemHistoryTimelineLoading()
                }

                is ItemHistoryTimelineState.Success -> {
                    ItemHistoryTimelineNodes(
                        modifier = Modifier.padding(horizontal = Spacing.medium),
                        shareId = shareId,
                        itemId = itemId,
                        itemRevisions = itemRevisions,
                        colors = itemColors,
                        onNavigated = onNavigated
                    )
                }
            }
        }
    }
}
