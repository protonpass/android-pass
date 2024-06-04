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

package proton.android.pass.features.item.details.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsContent
import proton.android.pass.composecomponents.impl.item.details.PassItemDetailsUiEvent
import proton.android.pass.composecomponents.impl.loading.PassFullScreenLoading
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.features.item.details.presentation.ItemDetailsState

@Composable
internal fun ItemDetailsContent(
    modifier: Modifier = Modifier,
    onEvent: (ItemDetailsUiEvent) -> Unit,
    state: ItemDetailsState
) = with(state) {
    when (this) {
        ItemDetailsState.Error -> {

        }

        ItemDetailsState.Loading -> {
            PassFullScreenLoading()
        }

        is ItemDetailsState.Success -> {
            val itemColors = passItemColors(itemCategory = itemDetailState.itemCategory)

            PassItemDetailsContent(
                modifier = modifier,
                itemDetailState = itemDetailState,
                itemColors = itemColors,
                topBar = {
                    ItemDetailsTopBar(
                        isLoading = false,
                        itemColors = itemColors,
                        actions = itemActions,
                        onUpClick = { onEvent(ItemDetailsUiEvent.OnNavigateBack) },
                        onEditClick = {},
                        onOptionsClick = {},
                        onShareClick = {}
                    )
                },
                onEvent = { uiEvent ->
                    when (uiEvent) {
                        is PassItemDetailsUiEvent.OnHiddenSectionClick -> TODO()
                        is PassItemDetailsUiEvent.OnHiddenSectionToggle -> TODO()
                        is PassItemDetailsUiEvent.OnLinkClick -> TODO()
                        is PassItemDetailsUiEvent.OnPasskeyClick -> TODO()
                        is PassItemDetailsUiEvent.OnSectionClick -> TODO()
                    }
                }
            )
        }
    }
}
