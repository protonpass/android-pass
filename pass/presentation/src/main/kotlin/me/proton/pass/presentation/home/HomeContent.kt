package me.proton.pass.presentation.home

import androidx.activity.compose.BackHandler
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
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.PassFloatingActionButton
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.shared.ConfirmItemDeletionDialog

@Suppress("LongParameterList")
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    homeScreenNavigation: HomeScreenNavigation,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    sendItemToTrash: (ItemUiModel) -> Unit,
    onDrawerIconClick: () -> Unit,
    onMoreOptionsClick: () -> Unit,
    onAddItemClick: (Option<ShareId>) -> Unit,
    onRefresh: () -> Unit
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
        Home(
            modifier = Modifier.padding(contentPadding),
            items = uiState.homeListUiState.items,
            highlight = uiState.searchUiState.searchQuery,
            onItemClick = { item ->
                keyboardController?.hide()
                homeScreenNavigation.toItemDetail(item.shareId, item.id)
            },
            navigation = homeScreenNavigation,
            onDeleteItemClicked = { itemToDelete = it },
            isLoading = uiState.homeListUiState.isLoading,
            isRefreshing = uiState.homeListUiState.isRefreshing,
            onRefresh = onRefresh
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
