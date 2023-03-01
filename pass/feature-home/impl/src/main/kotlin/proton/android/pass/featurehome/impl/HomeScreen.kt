package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.dialogs.ConfirmMoveItemToTrashDialog
import proton.android.pass.featurehome.impl.bottomsheet.AliasOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.LoginOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.NoteOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.SortingBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.password.GeneratePasswordBottomSheet
import proton.pass.domain.ItemType

@OptIn(
    ExperimentalLifecycleComposeApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Suppress("ComplexMethod")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenNavigation: HomeScreenNavigation,
    homeItemTypeSelection: HomeItemTypeSelection,
    homeVaultSelection: HomeVaultSelection,
    onDrawerIconClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val (currentBottomSheet, setBottomSheet) = rememberSaveable { mutableStateOf(HomeBottomSheetType.CreateItem) }
    val (selectedItem, setSelectedItem) = rememberSaveable { mutableStateOf<ItemUiModel?>(null) }
    val (shouldScrollToTop, setScrollToTop) = remember { mutableStateOf(false) }
    val (shouldShowDeleteDialog, setShowDeleteDialog) = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(homeItemTypeSelection) {
        viewModel.setItemTypeSelection(homeItemTypeSelection)
    }
    LaunchedEffect(homeVaultSelection) {
        viewModel.setVaultSelection(homeVaultSelection)
    }

    val bottomSheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()

    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            when (currentBottomSheet) {
                HomeBottomSheetType.CreateItem -> FABBottomSheetContents(
                    onCreateLogin = {
                        scope.launch {
                            bottomSheetState.hide()
                            homeScreenNavigation.toCreateLogin(uiState.homeListUiState.selectedShare)
                        }
                    },
                    onCreateAlias = {
                        scope.launch {
                            bottomSheetState.hide()
                            homeScreenNavigation.toCreateAlias(uiState.homeListUiState.selectedShare)
                        }
                    },
                    onCreateNote = {
                        scope.launch {
                            bottomSheetState.hide()
                            homeScreenNavigation.toCreateNote(uiState.homeListUiState.selectedShare)
                        }
                    },
                    onCreatePassword = { setBottomSheet(HomeBottomSheetType.GeneratePassword) }
                )
                HomeBottomSheetType.Sorting -> SortingBottomSheetContents(
                    sortingType = uiState.homeListUiState.sortingType
                ) {
                    viewModel.onSortingTypeChanged(it)
                    setScrollToTop(true)
                    scope.launch { bottomSheetState.hide() }
                }
                HomeBottomSheetType.LoginOptions -> LoginOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    onCopyUsername = {
                        scope.launch { bottomSheetState.hide() }
                        viewModel.copyToClipboard(it, HomeClipboardType.Username)
                    },
                    onCopyPassword = {
                        scope.launch { bottomSheetState.hide() }
                        viewModel.copyToClipboard(
                            text = it,
                            HomeClipboardType.Password
                        )
                    },
                    onEdit = { shareId, itemId ->
                        scope.launch { bottomSheetState.hide() }
                        homeScreenNavigation.toEditLogin(shareId, itemId)
                    },
                    onMoveToTrash = {
                        scope.launch { bottomSheetState.hide() }
                        setShowDeleteDialog(true)
                    }
                )
                HomeBottomSheetType.AliasOptions -> AliasOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    onCopyAlias = {
                        scope.launch { bottomSheetState.hide() }
                        viewModel.copyToClipboard(it, HomeClipboardType.Alias)
                    },
                    onEdit = { shareId, itemId ->
                        scope.launch { bottomSheetState.hide() }
                        homeScreenNavigation.toEditAlias(shareId, itemId)
                    },
                    onMoveToTrash = {
                        scope.launch { bottomSheetState.hide() }
                        setShowDeleteDialog(true)
                    }
                )
                HomeBottomSheetType.NoteOptions -> NoteOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    onCopyNote = {
                        scope.launch { bottomSheetState.hide() }
                        viewModel.copyToClipboard(it, HomeClipboardType.Note)
                    },
                    onEdit = { shareId, itemId ->
                        scope.launch { bottomSheetState.hide() }
                        homeScreenNavigation.toEditNote(shareId, itemId)
                    },
                    onMoveToTrash = {
                        scope.launch { bottomSheetState.hide() }
                        setShowDeleteDialog(true)
                    }
                )
                HomeBottomSheetType.GeneratePassword -> GeneratePasswordBottomSheet {
                    scope.launch { bottomSheetState.hide() }
                }
            }
        }
    ) {
        Box(modifier = modifier) {
            HomeContent(
                modifier = Modifier,
                uiState = uiState,
                homeFilter = homeItemTypeSelection,
                shouldScrollToTop = shouldScrollToTop,
                homeScreenNavigation = homeScreenNavigation,
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                onEnterSearch = { viewModel.onEnterSearch() },
                onStopSearching = { viewModel.onStopSearching() },
                sendItemToTrash = { viewModel.sendItemToTrash(it) },
                onDrawerIconClick = onDrawerIconClick,
                onMoreOptionsClick = {
                    setBottomSheet(HomeBottomSheetType.Sorting)
                    scope.launch { bottomSheetState.show() }
                },
                onAddItemClick = {
                    setBottomSheet(HomeBottomSheetType.CreateItem)
                    scope.launch { bottomSheetState.show() }
                },
                onItemMenuClick = { item ->
                    setSelectedItem(item)
                    when (item.itemType) {
                        is ItemType.Alias -> setBottomSheet(HomeBottomSheetType.AliasOptions)
                        is ItemType.Login -> setBottomSheet(HomeBottomSheetType.LoginOptions)
                        is ItemType.Note -> setBottomSheet(HomeBottomSheetType.NoteOptions)
                        ItemType.Password -> {}
                    }
                    scope.launch { bottomSheetState.show() }
                },
                onRefresh = { viewModel.onRefresh() },
                onScrollToTop = { setScrollToTop(false) }
            )

            ConfirmMoveItemToTrashDialog(
                itemName = selectedItem?.name ?: "",
                show = shouldShowDeleteDialog,
                onConfirm = {
                    viewModel.sendItemToTrash(selectedItem)
                    setShowDeleteDialog(false)
                },
                onDismiss = { setShowDeleteDialog(false) },
                onCancel = { setShowDeleteDialog(false) }
            )
        }
    }
}
