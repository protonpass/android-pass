package me.proton.pass.autofill.ui.autofill.select

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsRefreshingState

class SelectItemUiStatePreviewProvider : PreviewParameterProvider<SelectItemUiState> {
    override val values: Sequence<SelectItemUiState>
        get() = sequenceOf(
            SelectItemUiState(
                SelectItemListUiState(
                    isLoading = IsLoadingState.Loading,
                    isRefreshing = IsRefreshingState.NotRefreshing,
                    items = emptyList(),
                    itemClickedEvent = ItemClickedEvent.None
                ),
                SearchUiState.Initial
            ),
            SelectItemUiState(
                SelectItemListUiState(
                    isLoading = IsLoadingState.NotLoading,
                    isRefreshing = IsRefreshingState.NotRefreshing,
                    items = listOf(
                        ItemUiModel(
                            id = ItemId("123"),
                            shareId = ShareId("345"),
                            name = "Item with long text",
                            note = "Note content",
                            itemType = ItemType.Note(
                                "Some very very long test that should be ellipsized as we type"
                            )
                        )
                    ),
                    itemClickedEvent = ItemClickedEvent.None
                ),
                SearchUiState.Initial
            ),
            SelectItemUiState(
                SelectItemListUiState.Loading,
                SearchUiState(
                    searchQuery = "query",
                    inSearchMode = true
                )
            )
        )
}
