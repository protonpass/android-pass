package me.proton.android.pass.ui.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE
import android.view.autofill.AutofillManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefreshState
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.ConfirmItemDeletionDialog
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.PassSwipeRefresh
import me.proton.core.pass.presentation.components.common.item.ItemAction
import me.proton.core.pass.presentation.components.common.item.ItemsList
import me.proton.core.pass.presentation.components.model.ItemUiModel
import me.proton.core.pass.presentation.uievents.IsLoadingState

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenNavigation: HomeScreenNavigation,
    onDrawerIconClick: () -> Unit
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    RequestAutofillIfSupported()
    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            BottomSheetContents(
                state = bottomSheetState,
                navigation = homeScreenNavigation,
                shareId = uiState.homeListUiState.selectedShare
            )
        }
    ) {
        HomeContent(
            modifier = modifier,
            uiState = uiState,
            homeScreenNavigation = homeScreenNavigation,
            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
            onEnterSearch = { viewModel.onEnterSearch() },
            onStopSearching = { viewModel.onStopSearching() },
            sendItemToTrash = { viewModel.sendItemToTrash(it) },
            onDrawerIconClick = onDrawerIconClick,
            onAddItemClick = { coroutineScope.launch { bottomSheetState.show() } },
            onRefresh = { viewModel.onRefresh() }
        )
    }
}

@Suppress("LongParameterList")
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
private fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    homeScreenNavigation: HomeScreenNavigation,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    sendItemToTrash: (ItemUiModel) -> Unit,
    onDrawerIconClick: () -> Unit,
    onAddItemClick: (ShareId?) -> Unit,
    onRefresh: () -> Unit
) {

    // Only enable the backhandler if we are in search mode
    BackHandler(enabled = uiState.searchUiState.inSearchMode) {
        onStopSearching()
    }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            HomeTopBar(
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                onSearchQueryChange = onSearchQueryChange,
                onEnterSearch = onEnterSearch,
                onStopSearching = onStopSearching,
                onDrawerIconClick = onDrawerIconClick,
                onAddItemClick = { onAddItemClick(uiState.homeListUiState.selectedShare) }
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

            if (uiState.homeListUiState.errorMessage != null) {
                Text("Something went boom: ${uiState.homeListUiState.errorMessage}")
            }
        }
    }
}

@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
private fun HomeTopBar(
    searchQuery: String,
    inSearchMode: Boolean,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    onDrawerIconClick: () -> Unit,
    onAddItemClick: () -> Unit
) {
    if (inSearchMode) {
        SearchHomeTopBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onStopSearch = {
                onStopSearching()
            }
        )
    } else {
        IdleHomeTopBar(
            startSearchMode = { onEnterSearch() },
            onDrawerIconClick = onDrawerIconClick,
            onAddItemClick = onAddItemClick
        )
    }
}

@Composable
private fun Home(
    items: List<ItemUiModel>,
    modifier: Modifier = Modifier,
    onItemClick: (ItemUiModel) -> Unit,
    navigation: HomeScreenNavigation,
    onDeleteItemClicked: (ItemUiModel) -> Unit,
    isRefreshing: IsLoadingState,
    onRefresh: () -> Unit
) {
    PassSwipeRefresh(
        state = SwipeRefreshState(isRefreshing is IsLoadingState.Loading),
        onRefresh = onRefresh
    ) {
        if (items.isNotEmpty()) {
            ItemsList(
                items = items,
                modifier = modifier,
                onItemClick = onItemClick,
                itemActions = listOf(
                    ItemAction(
                        onSelect = { goToEdit(navigation, it) },
                        title = R.string.action_edit_placeholder,
                        icon = me.proton.core.presentation.R.drawable.ic_proton_eraser,
                        textColor = ProtonTheme.colors.textNorm
                    ),
                    ItemAction(
                        onSelect = { onDeleteItemClicked(it) },
                        title = R.string.action_move_to_trash,
                        icon = me.proton.core.presentation.R.drawable.ic_proton_trash,
                        textColor = ProtonTheme.colors.notificationError
                    )
                )
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = stringResource(R.string.message_no_saved_credentials),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

internal fun goToEdit(
    navigation: HomeScreenNavigation,
    item: ItemUiModel
) {
    when (item.itemType) {
        is ItemType.Login -> navigation.toEditLogin(item.shareId, item.id)
        is ItemType.Note -> navigation.toEditNote(item.shareId, item.id)
        is ItemType.Alias -> navigation.toEditAlias(item.shareId, item.id)
        ItemType.Password -> {}
    }
}

@Composable
private fun RequestAutofillIfSupported() {
    val context = LocalContext.current
    val autofillManager = context.getSystemService(AutofillManager::class.java)

    // We are only requesting Autofill if the user does not have any autofill provider selected
    // We should investigate a way for inviting the user to select our app even if they have another one,
    // probably in an onboarding process or similar flow
    if (!autofillManager.isEnabled) {
        LaunchedEffect(true) {
            val intent = Intent(ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }
    }
}
