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

package proton.android.pass.autofill.ui.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.Clock
import proton.android.pass.autofill.ui.autofill.select.AutofillItemClickedEvent
import proton.android.pass.autofill.ui.autofill.select.SearchInMode
import proton.android.pass.autofill.ui.autofill.select.SearchUiState
import proton.android.pass.autofill.ui.autofill.select.SelectItemListItems
import proton.android.pass.autofill.ui.autofill.select.SelectItemListUiState
import proton.android.pass.autofill.ui.autofill.select.SelectItemUiState
import proton.android.pass.commonui.api.GroupedItemList
import proton.android.pass.commonui.api.GroupingKeys
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsProcessingSearchState
import proton.android.pass.composecomponents.impl.uievents.IsRefreshingState
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

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
                    canUpgrade = false
                ),
                SearchUiState.Initial,
                isRemovePrimaryVaultEnabled = false
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
                    canUpgrade = false
                ),
                SearchUiState.Initial,
                isRemovePrimaryVaultEnabled = false
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
                    canUpgrade = false
                ),
                SearchUiState(
                    searchQuery = "query",
                    inSearchMode = true,
                    isProcessingSearch = IsProcessingSearchState.NotLoading,
                    searchInMode = SearchInMode.AllVaults
                ),
                isRemovePrimaryVaultEnabled = false
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
        lastAutofillTime = Clock.System.now()
    )
}
