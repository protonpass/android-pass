package me.proton.android.pass.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
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
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.ConfirmItemDeletionDialog
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.components.common.PassFloatingActionButton
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState

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
                onMoreOptionsClick = { } // Not implemented yet
            )
        }
    ) { contentPadding ->
        Box {
            when (uiState.homeListUiState.isLoading) {
                IsLoadingState.Loading -> LoadingDialog()
                IsLoadingState.NotLoading -> {
                    var itemToDelete by remember { mutableStateOf<ItemUiModel?>(null) }
                    val keyboardController = LocalSoftwareKeyboardController.current
                    Home(
                        items = uiState.homeListUiState.items,
                        highlight = uiState.searchUiState.searchQuery,
                        modifier = Modifier.padding(contentPadding),
                        onItemClick = { item ->
                            keyboardController?.hide()
                            homeScreenNavigation.toItemDetail(item.shareId, item.id)
                        },
                        navigation = homeScreenNavigation,
                        onDeleteItemClicked = { itemToDelete = it },
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
        }
    }
}
