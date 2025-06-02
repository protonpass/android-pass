/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.selectitem.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.features.selectitem.navigation.SelectItemNavigation
import proton.android.pass.features.selectitem.navigation.SelectItemState
import proton.android.pass.features.selectitem.presentation.SelectItemViewModel

@Composable
fun SelectItemScreen(
    modifier: Modifier = Modifier,
    state: SelectItemState,
    onScreenShown: () -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit,
    viewModel: SelectItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        onScreenShown()
        viewModel.setInitialState(state)
    }

    OnItemSelectLaunchEffect(
        event = uiState.listUiState.itemClickedEvent,
        clearEvent = { viewModel.clearEvent() },
        onNavigate = onNavigate
    )

    SelectItemScreenContent(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is SelectItemEvent.ItemClicked -> if (event.isSuggestion) {
                    viewModel.onSuggestionClicked(event.item)
                } else {
                    viewModel.onItemClicked(event.item)
                }

                is SelectItemEvent.ItemOptionsClicked -> onNavigate(
                    SelectItemNavigation.ItemOptions(
                        event.item.userId,
                        event.item.shareId,
                        event.item.id
                    )
                )

                is SelectItemEvent.SearchQueryChange -> viewModel.onSearchQueryChange(event.query)
                SelectItemEvent.EnterSearch -> viewModel.onEnterSearch()
                SelectItemEvent.StopSearching -> viewModel.onStopSearching()
                SelectItemEvent.ScrolledToTop -> viewModel.onScrolledToTop()
                SelectItemEvent.SeeAllPinned -> viewModel.onEnterSeeAllPinsMode()
                SelectItemEvent.StopPinningMode -> viewModel.onStopPinningMode()
                is SelectItemEvent.SwitchAccount -> viewModel.onAccountSwitch(event.userId)
            }
        },
        onNavigate = onNavigate
    )
}

@Composable
private fun OnItemSelectLaunchEffect(
    event: AutofillItemClickedEvent,
    clearEvent: () -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit
) {
    LaunchedEffect(event) {
        when (event) {
            is AutofillItemClickedEvent.ItemClicked -> {
                onNavigate(SelectItemNavigation.ItemSelected(event.item))
            }

            is AutofillItemClickedEvent.SuggestionClicked -> {
                onNavigate(SelectItemNavigation.SuggestionSelected(event.item))

            }

            else -> Unit
        }
        clearEvent()
    }

}
