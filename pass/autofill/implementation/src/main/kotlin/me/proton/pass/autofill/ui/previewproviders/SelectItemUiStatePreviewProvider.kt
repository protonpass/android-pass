package me.proton.pass.autofill.ui.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.autofill.ui.autofill.select.ItemClickedEvent
import me.proton.pass.autofill.ui.autofill.select.SearchUiState
import me.proton.pass.autofill.ui.autofill.select.SelectItemListItems
import me.proton.pass.autofill.ui.autofill.select.SelectItemListUiState
import me.proton.pass.autofill.ui.autofill.select.SelectItemUiState
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsProcessingSearchState
import me.proton.pass.presentation.uievents.IsRefreshingState

class SelectItemUiStatePreviewProvider : PreviewParameterProvider<SelectItemUiState> {
    override val values: Sequence<SelectItemUiState>
        get() = sequenceOf(
            SelectItemUiState(
                SelectItemListUiState(
                    isLoading = IsLoadingState.NotLoading,
                    isRefreshing = IsRefreshingState.NotRefreshing,
                    items = SelectItemListItems(
                        suggestions = emptyList(),
                        items = listOf(item("Item with long text")),
                        suggestionsForTitle = ""
                    ),
                    itemClickedEvent = ItemClickedEvent.None
                ),
                SearchUiState.Initial
            ),
            SelectItemUiState(
                SelectItemListUiState(
                    isLoading = IsLoadingState.NotLoading,
                    isRefreshing = IsRefreshingState.NotRefreshing,
                    items = SelectItemListItems(
                        suggestions = listOf(item("Suggested item")),
                        items = listOf(
                            item("Item with long text"),
                            item("Another item")
                        ),
                        suggestionsForTitle = "some.website.local"
                    ),
                    itemClickedEvent = ItemClickedEvent.None
                ),
                SearchUiState.Initial
            ),
            SelectItemUiState(
                SelectItemListUiState(
                    isLoading = IsLoadingState.NotLoading,
                    isRefreshing = IsRefreshingState.NotRefreshing,
                    items = SelectItemListItems.Initial,
                    itemClickedEvent = ItemClickedEvent.None
                ),
                SearchUiState(
                    searchQuery = "query",
                    inSearchMode = true,
                    isProcessingSearch = IsProcessingSearchState.NotLoading
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
        )
    )
}
