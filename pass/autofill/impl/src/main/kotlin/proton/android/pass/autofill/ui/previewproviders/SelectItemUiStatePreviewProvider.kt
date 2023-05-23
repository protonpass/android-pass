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
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
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
                SearchUiState.Initial
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
                SearchUiState.Initial
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
                )
            )
        )

    private fun item(name: String): ItemUiModel = ItemUiModel(
        id = ItemId(name),
        shareId = ShareId("345"),
        name = name,
        note = "Note content",
        itemType = ItemType.Note(
            "Some very very long test that should be ellipsized as we type"
        ),
        state = 0,
        createTime = Clock.System.now(),
        modificationTime = Clock.System.now(),
        lastAutofillTime = Clock.System.now()
    )
}
