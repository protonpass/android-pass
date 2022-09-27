package me.proton.android.pass.ui.home

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.ConfirmItemDeletionDialog
import me.proton.android.pass.ui.shared.ItemAction
import me.proton.android.pass.ui.shared.ItemsList
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.compose.component.ProtonModalBottomSheetLayout
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.model.ItemUiModel

internal typealias OnItemClick = (ShareId, ItemId) -> Unit

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenNavigation: HomeScreenNavigation,
    onDrawerIconClick: () -> Unit
) {
    val viewModel = hiltViewModel<HomeViewModel>()
    val uiState by viewModel.homeUiState.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val coroutineScope = rememberCoroutineScope()

    RequestAutofillIfSupported()
    val selectedShare = if (uiState is HomeUiState.Content) {
        (uiState as HomeUiState.Content).selectedShare
    } else null
    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            BottomSheetContents(
                state = bottomSheetState,
                navigation = homeScreenNavigation,
                shareId = selectedShare
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
            onAddItemClick = { coroutineScope.launch { bottomSheetState.show() } }
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
    onAddItemClick: (ShareId?) -> Unit
) {

    val selectedShare = if (uiState is HomeUiState.Content) uiState.selectedShare else null
    val backHandlerEnabled = (uiState as? HomeUiState.Content)?.inSearchMode ?: false
    BackHandler(enabled = backHandlerEnabled) {
        onStopSearching()
    }

    Scaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        topBar = {
            val searchQuery = (uiState as? HomeUiState.Content)?.searchQuery.orEmpty()
            val inSearchMode = (uiState as? HomeUiState.Content)?.inSearchMode ?: false

            HomeTopBar(
                searchQuery = searchQuery,
                inSearchMode = inSearchMode,
                onSearchQueryChange = onSearchQueryChange,
                onEnterSearch = onEnterSearch,
                onStopSearching = onStopSearching,
                onDrawerIconClick = onDrawerIconClick,
                onAddItemClick = { onAddItemClick(selectedShare) }
            )
        }
    ) { contentPadding ->
        Box {
            when (uiState) {
                is HomeUiState.Loading -> LoadingDialog()
                is HomeUiState.Content -> {
                    var itemToDelete by remember { mutableStateOf<ItemUiModel?>(null) }
                    val keyboardController = LocalSoftwareKeyboardController.current
                    Home(
                        items = uiState.items,
                        modifier = Modifier.padding(contentPadding),
                        onItemClick = { shareId, itemId ->
                            keyboardController?.hide()
                            homeScreenNavigation.toItemDetail(shareId, itemId)
                        },
                        navigation = homeScreenNavigation,
                        onDeleteItemClicked = { itemToDelete = it }
                    )
                    ConfirmItemDeletionDialog(
                        state = itemToDelete,
                        onDismiss = { itemToDelete = null },
                        title = R.string.alert_confirm_item_send_to_trash_title,
                        message = R.string.alert_confirm_item_send_to_trash_message,
                        onConfirm = sendItemToTrash
                    )
                }
                is HomeUiState.Error -> Text("Something went boom: ${uiState.message}")
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
    onItemClick: OnItemClick,
    navigation: HomeScreenNavigation,
    onDeleteItemClicked: (ItemUiModel) -> Unit
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
                    icon = R.drawable.ic_proton_eraser,
                    textColor = ProtonTheme.colors.textNorm
                ),
                ItemAction(
                    onSelect = { onDeleteItemClicked(it) },
                    title = R.string.action_move_to_trash,
                    icon = R.drawable.ic_proton_trash,
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

internal fun goToEdit(
    navigation: HomeScreenNavigation,
    item: ItemUiModel
) {
    when (item.itemType) {
        is ItemType.Login -> navigation.toEditLogin(item.shareId, item.id)
        is ItemType.Note -> navigation.toEditNote(item.shareId, item.id)
        is ItemType.Alias -> navigation.toEditAlias(item.shareId, item.id)
    }
}

@Composable
private fun RequestAutofillIfSupported() {
//    if (Build.VERSION.SDK_INT >= 9000) {
//        val context = LocalContext.current
//        RequestAutofillAccessIfNeeded(context = context)
//    }
}

/*
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun RequestAutofillAccessIfNeeded(context: Context) {
    val autofillManager = context.getSystemService(AutofillManager::class.java)
    if (!autofillManager.hasEnabledAutofillServices()) {
        LaunchedEffect(true) {
            val intent = Intent(ACTION_REQUEST_SET_AUTOFILL_SERVICE)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }
    }
}
*/
