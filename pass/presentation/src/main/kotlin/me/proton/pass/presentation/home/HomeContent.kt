package me.proton.pass.presentation.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.android.pass.composecomponents.impl.buttons.PassFloatingActionButton
import me.proton.android.pass.composecomponents.impl.item.ItemsList
import me.proton.android.pass.composecomponents.impl.dialogs.ConfirmItemDeletionDialog
import me.proton.android.pass.composecomponents.impl.item.EmptyList

@Suppress("LongParameterList")
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    homeFilter: HomeFilterMode,
    shouldScrollToTop: Boolean,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    homeScreenNavigation: HomeScreenNavigation,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    sendItemToTrash: (ItemUiModel) -> Unit,
    onDrawerIconClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    onAddItemClick: (Option<ShareId>) -> Unit,
    onItemMenuClick: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit,
    onScrollToTop: () -> Unit
) {
    // Only enable the backhandler if we are in search mode
    BackHandler(enabled = uiState.searchUiState.inSearchMode) {
        onStopSearching()
    }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        floatingActionButton = {
            PassFloatingActionButton(
                onClick = { onAddItemClick(uiState.homeListUiState.selectedShare) }
            )
        },
        topBar = {
            HomeTopBar(
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                homeFilter = homeFilter,
                onSearchQueryChange = onSearchQueryChange,
                onEnterSearch = onEnterSearch,
                onStopSearching = onStopSearching,
                onDrawerIconClick = onDrawerIconClick,
                onMoreOptionsClick = onMoreOptionsClick
            )
        }
    ) { contentPadding ->
        var itemToDelete by remember { mutableStateOf<ItemUiModel?>(null) }
        val keyboardController = LocalSoftwareKeyboardController.current
        ItemsList(
            modifier = Modifier.padding(contentPadding),
            items = uiState.homeListUiState.items,
            shouldScrollToTop = shouldScrollToTop,
            highlight = uiState.searchUiState.searchQuery,
            onItemClick = { item ->
                keyboardController?.hide()
                homeScreenNavigation.toItemDetail(item.shareId, item.id)
            },
            onItemMenuClick = onItemMenuClick,
            isLoading = uiState.homeListUiState.isLoading,
            isProcessingSearch = uiState.searchUiState.isProcessingSearch,
            isRefreshing = uiState.homeListUiState.isRefreshing,
            onRefresh = onRefresh,
            onScrollToTop = onScrollToTop,
            emptyContent = {
                if (uiState.searchUiState.inSearchMode) {
                    EmptySearchResults()
                } else {
                    EmptyList(emptyListMessage = stringResource(id = R.string.empty_list_home_subtitle))
                }
            },
            header = {
                item { AutofillCard(Modifier.fillMaxWidth()) }
            },
            footer = {
                item { Spacer(Modifier.height(64.dp)) }
            }
        )
        ConfirmItemDeletionDialog(
            state = itemToDelete,
            onDismiss = { itemToDelete = null },
            title = R.string.alert_confirm_item_send_to_trash_title,
            message = R.string.alert_confirm_item_send_to_trash_message,
            onConfirm = sendItemToTrash
        )
    }
}
