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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.buttons.PassFloatingActionButton
import proton.android.pass.composecomponents.impl.pinning.PinCarousel
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.featureselectitem.R
import proton.android.pass.featureselectitem.navigation.SelectItemNavigation

@Suppress("ComplexMethod")
@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    onItemClicked: (ItemUiModel) -> Unit,
    onItemOptionsClicked: (ItemUiModel) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    onStopPinningMode: () -> Unit,
    onScrolledToTop: () -> Unit,
    onSeeAllPinned: () -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit
) {
    val verticalScroll = rememberLazyListState()
    var showFab by remember { mutableStateOf(true) }
    val isPinningOrSearch =
        remember(uiState.pinningUiState.inPinningMode, uiState.searchUiState.inSearchMode) {
            uiState.pinningUiState.inPinningMode || uiState.searchUiState.inSearchMode
        }

    LaunchedEffect(verticalScroll) {
        var prev = 0
        snapshotFlow { verticalScroll.firstVisibleItemIndex }
            .collect {
                showFab = it <= prev
                prev = it
            }
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            PassFloatingActionButton(
                visible = showFab,
                onClick = { onNavigate(SelectItemNavigation.AddItem) }
            )
        },
        topBar = {
            val placeholder = if (!uiState.pinningUiState.inPinningMode) {
                when (uiState.searchUiState.searchInMode) {
                    SearchInMode.AllVaults -> stringResource(id = R.string.topbar_search_query)
                    SearchInMode.OldestVaults -> stringResource(id = R.string.topbar_search_query_oldest_vaults)
                    SearchInMode.Uninitialized -> stringResource(id = R.string.topbar_search_query_uninitialized)
                }
            } else {
                stringResource(id = R.string.topbar_search_pinned_items)
            }

            SearchTopBar(
                placeholderText = placeholder,
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                onSearchQueryChange = onSearchQueryChange,
                onStopSearch = onStopSearching,
                onEnterSearch = onEnterSearch,
                drawerIcon = {
                    BackArrowCircleIconButton(
                        color = PassTheme.colors.loginInteractionNorm,
                        backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                        onUpClick = {
                            when {
                                uiState.searchUiState.inSearchMode -> {
                                    onStopSearching()
                                }
                                uiState.pinningUiState.inPinningMode -> {
                                    onStopPinningMode()
                                }
                                else -> {
                                    onNavigate(SelectItemNavigation.Cancel)
                                }
                            }
                        }
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            if (!isPinningOrSearch) {
                PinCarousel(
                    modifier = Modifier.height(48.dp),
                    list = uiState.pinningUiState.unFilteredItems,
                    canLoadExternalImages = uiState.listUiState.canLoadExternalImages,
                    onItemClick = onItemClicked,
                    onSeeAllClick = onSeeAllPinned,
                )

                if (uiState.pinningUiState.unFilteredItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.medium))
                }
            }
            SelectItemList(
                uiState = uiState,
                scrollState = verticalScroll,
                onScrolledToTop = onScrolledToTop,
                onItemOptionsClicked = onItemOptionsClicked,
                onItemClicked = onItemClicked,
                onNavigate = onNavigate
            )
        }
    }
}
