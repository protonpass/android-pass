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

package proton.android.pass.featureselectitem.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.Clock
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.featureselectitem.ui.AutofillItemClickedEvent
import proton.android.pass.featureselectitem.ui.PinningUiState
import proton.android.pass.featureselectitem.ui.SearchInMode
import proton.android.pass.featureselectitem.ui.SearchUiState
import proton.android.pass.featureselectitem.ui.SelectItemListItems
import proton.android.pass.featureselectitem.ui.SelectItemListUiState
import proton.android.pass.featureselectitem.ui.SelectItemUiState

class SelectItemUiStatePreviewProvider : PreviewParameterProvider<SelectItemUiState> {
    override val values: Sequence<SelectItemUiState>
        get() = sequenceOf(
            SelectItemUiState(
                SelectItemListUiState(
                    isLoading = IsLoadingState.NotLoading,
                    isRefreshing = IsRefreshingState.NotRefreshing,
                    itemClickedEvent = AutofillItemClickedEvent.None,
                    items = SelectItemListItems(
                        suggestions = persistentListOf(),
                        items = persistentListOf(
                            GroupedItemList(
                                key = GroupingKeys.NoGrouping,
                                items = persistentListOf(item("Item with long text"))
                            )
                        ),
                        suggestionsForTitle = ""
                    ),
                    shares = persistentMapOf(),
                    sortingType = SearchSortingType.MostRecent,
                    shouldScrollToTop = false,
                    canLoadExternalImages = false,
                    displayOnlyPrimaryVaultMessage = false,
                    canUpgrade = false,
                    displayCreateButton = true
                ),
                SearchUiState.Initial,
                PinningUiState.Initial
            ),
            SelectItemUiState(
                SelectItemListUiState(
                    isLoading = IsLoadingState.NotLoading,
                    isRefreshing = IsRefreshingState.NotRefreshing,
                    itemClickedEvent = AutofillItemClickedEvent.None,
                    items = SelectItemListItems(
                        suggestions = persistentListOf(item("Suggested item")),
                        items = persistentListOf(
                            GroupedItemList(
                                key = GroupingKeys.NoGrouping,
                                items = persistentListOf(
                                    item("Item with long text"),
                                    item("Another item")
                                )
                            )
                        ),
                        suggestionsForTitle = "some.website.local"
                    ),
                    shares = persistentMapOf(),
                    sortingType = SearchSortingType.MostRecent,
                    shouldScrollToTop = false,
                    canLoadExternalImages = false,
                    displayOnlyPrimaryVaultMessage = true,
                    canUpgrade = false,
                    displayCreateButton = true
                ),
                SearchUiState.Initial,
                PinningUiState.Initial
            ),
            SelectItemUiState(
                SelectItemListUiState(
                    isLoading = IsLoadingState.NotLoading,
                    isRefreshing = IsRefreshingState.NotRefreshing,
                    itemClickedEvent = AutofillItemClickedEvent.None,
                    items = SelectItemListItems.Initial,
                    shares = persistentMapOf(),
                    sortingType = SearchSortingType.MostRecent,
                    shouldScrollToTop = false,
                    canLoadExternalImages = false,
                    displayOnlyPrimaryVaultMessage = false,
                    canUpgrade = false,
                    displayCreateButton = true
                ),
                SearchUiState(
                    searchQuery = "query",
                    inSearchMode = true,
                    isProcessingSearch = IsProcessingSearchState.NotLoading,
                    searchInMode = SearchInMode.AllVaults
                ),
                PinningUiState.Initial
            )
        )

    private fun item(name: String): ItemUiModel = ItemUiModel(
        id = ItemId(name),
        shareId = ShareId("345"),
        contents = ItemContents.Note(
            name,
            "Some very very long test that should be ellipsized as we type"
        ),
        state = 0,
        createTime = Clock.System.now(),
        modificationTime = Clock.System.now(),
        lastAutofillTime = Clock.System.now(),
        isPinned = false
    )
}
