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

package proton.android.pass.featureselectitem.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featureselectitem.navigation.SelectItemNavigation
import proton.android.pass.featureselectitem.navigation.SelectItemState
import proton.android.pass.featureselectitem.presentation.SelectItemViewModel

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
        onItemClicked = { item -> viewModel.onItemClicked(item) },
        onItemOptionsClicked = { item ->
            onNavigate(SelectItemNavigation.ItemOptions(item.shareId, item.id))
        },
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onEnterSearch = { viewModel.onEnterSearch() },
        onStopSearching = { viewModel.onStopSearching() },
        onScrolledToTop = { viewModel.onScrolledToTop() },
        onSeeAllPinned = { viewModel.onEnterSeeAllPinsMode() },
        onStopPinningMode = { viewModel.onStopPinningMode() },
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
        if (event is AutofillItemClickedEvent.Clicked) {
            onNavigate(SelectItemNavigation.ItemSelected(event.item))
            clearEvent()
        }
    }

}
