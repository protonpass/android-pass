package proton.android.pass.featurehome.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.featurehome.impl.HomeBottomSheetType.AliasOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.LoginOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.NoteOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.TrashItemOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.TrashOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.VaultOptions
import proton.android.pass.featurehome.impl.HomeNavigation.SortingBottomsheet
import proton.android.pass.featurehome.impl.bottomsheet.AliasOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.LoginOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.NoteOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.TrashAllBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.VaultOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.saver.HomeBottomSheetTypeSaver
import proton.android.pass.featurehome.impl.trash.ConfirmClearTrashDialog
import proton.android.pass.featurehome.impl.trash.ConfirmRestoreAllDialog
import proton.android.pass.featurehome.impl.vault.VaultDrawerContent
import proton.android.pass.featurehome.impl.vault.VaultDrawerViewModel
import proton.android.pass.featuretrash.impl.ConfirmDeleteItemDialog
import proton.android.pass.featuretrash.impl.TrashItemBottomSheetContents
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@OptIn(
    ExperimentalLifecycleComposeApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Suppress("ComplexMethod")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateEvent: (HomeNavigation) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    vaultDrawerViewModel: VaultDrawerViewModel = hiltViewModel()
) {
    val homeUiState by homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val drawerUiState by vaultDrawerViewModel.drawerUiState.collectAsStateWithLifecycle()

    var currentBottomSheet by rememberSaveable(stateSaver = HomeBottomSheetTypeSaver) {
        mutableStateOf(TrashOptions)
    }
    var selectedItem by rememberSaveable(stateSaver = ItemUiModelSaver) {
        mutableStateOf(null)
    }
    var shouldShowDeleteItemDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowRestoreAllDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowClearTrashDialog by rememberSaveable { mutableStateOf(false) }
    var selectedShare: ShareUiModelWithItemCount? by rememberSaveable(stateSaver = ShareUiModelWithItemCountSaver) {
        mutableStateOf(null)
    }

    val actionState = homeUiState.homeListUiState.actionState
    LaunchedEffect(actionState) {
        if (actionState == ActionState.Done) {
            shouldShowDeleteItemDialog = false
            shouldShowRestoreAllDialog = false
            shouldShowClearTrashDialog = false
            homeViewModel.restoreActionState()
        }
    }

    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isTrashMode = homeUiState.homeListUiState.homeVaultSelection == HomeVaultSelection.Trash

    BackHandler(drawerState.isOpen || bottomSheetState.isVisible) {
        scope.launch {
            if (drawerState.isOpen) {
                drawerState.close()
            } else if (bottomSheetState.isVisible) {
                bottomSheetState.hide()
            }
        }
    }

    PassModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            when (currentBottomSheet) {
                LoginOptions -> LoginOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                    onCopyUsername = {
                        scope.launch { bottomSheetState.hide() }
                        homeViewModel.copyToClipboard(it, HomeClipboardType.Username)
                    },
                    onCopyPassword = {
                        scope.launch { bottomSheetState.hide() }
                        homeViewModel.copyToClipboard(
                            text = it,
                            HomeClipboardType.Password
                        )
                    },
                    onEdit = { shareId, itemId ->
                        scope.launch { bottomSheetState.hide() }
                        onNavigateEvent(HomeNavigation.EditLogin(shareId, itemId))
                    },
                    onMoveToTrash = {
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.sendItemToTrash(it)
                        }
                    },
                    onRemoveFromRecentSearch = { shareId, itemId ->
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.onClearRecentSearch(shareId, itemId)
                        }
                    }
                )

                AliasOptions -> AliasOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                    onCopyAlias = {
                        scope.launch { bottomSheetState.hide() }
                        homeViewModel.copyToClipboard(it, HomeClipboardType.Alias)
                    },
                    onEdit = { shareId, itemId ->
                        scope.launch { bottomSheetState.hide() }
                        onNavigateEvent(HomeNavigation.EditAlias(shareId, itemId))
                    },
                    onMoveToTrash = {
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.sendItemToTrash(it)
                        }
                    },
                    onRemoveFromRecentSearch = { shareId, itemId ->
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.onClearRecentSearch(shareId, itemId)
                        }
                    }
                )

                NoteOptions -> NoteOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                    onCopyNote = {
                        scope.launch { bottomSheetState.hide() }
                        homeViewModel.copyToClipboard(it, HomeClipboardType.Note)
                    },
                    onEdit = { shareId, itemId ->
                        scope.launch { bottomSheetState.hide() }
                        onNavigateEvent(HomeNavigation.EditNote(shareId, itemId))
                    },
                    onMoveToTrash = {
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.sendItemToTrash(it)
                        }
                    },
                    onRemoveFromRecentSearch = { shareId, itemId ->
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.onClearRecentSearch(shareId, itemId)
                        }
                    }
                )

                VaultOptions -> {
                    val showDelete = when (val share = homeUiState.homeListUiState.selectedShare) {
                        None -> {
                            // We are in the all vaults view, so we can't delete the primary vault
                            selectedShare?.isPrimary != true
                        }

                        is Some -> {
                            // We are in the vault view, so we can't delete the actively selected
                            // vault nor the primary one
                            val isSelectedVault = share.value.id.id == selectedShare?.id?.id
                            val isPrimaryVault = selectedShare?.isPrimary ?: false
                            !isSelectedVault && !isPrimaryVault
                        }
                    }
                    VaultOptionsBottomSheetContents(
                        showDelete = showDelete,
                        onEdit = {
                            scope.launch {
                                bottomSheetState.hide()
                                selectedShare?.let { onNavigateEvent(HomeNavigation.EditVault(it.id)) }
                            }
                        },
                        onRemove = {
                            scope.launch {
                                bottomSheetState.hide()
                                selectedShare?.let { onNavigateEvent(HomeNavigation.DeleteVault(it.id)) }
                            }
                        }
                    )
                }

                TrashItemOptions -> TrashItemBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    onRestoreItem = { shareId, itemId ->
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.restoreItem(shareId, itemId)
                        }
                    },
                    onDeleteItem = { _, _ ->
                        scope.launch {
                            bottomSheetState.hide()
                            shouldShowDeleteItemDialog = true
                        }
                    }
                )

                TrashOptions -> TrashAllBottomSheetContents(
                    onEmptyTrash = {
                        scope.launch {
                            bottomSheetState.hide()
                            shouldShowClearTrashDialog = true
                        }
                    },
                    onRestoreAll = {
                        scope.launch {
                            bottomSheetState.hide()
                            shouldShowRestoreAllDialog = true
                        }
                    }
                )
            }
        }
    ) {
        ModalDrawer(
            modifier = modifier,
            drawerState = drawerState,
            drawerShape = CutCornerShape(0.dp),
            scrimColor = PassTheme.colors.backdrop,
            drawerContent = {
                VaultDrawerContent(
                    homeVaultSelection = drawerUiState.vaultSelection,
                    list = drawerUiState.shares,
                    totalTrashedItems = drawerUiState.totalTrashedItems,
                    onAllVaultsClick = {
                        scope.launch { drawerState.close() }
                        vaultDrawerViewModel.setVaultSelection(HomeVaultSelection.AllVaults)
                        homeViewModel.setVaultSelection(HomeVaultSelection.AllVaults)
                    },
                    onVaultClick = {
                        scope.launch { drawerState.close() }
                        vaultDrawerViewModel.setVaultSelection(HomeVaultSelection.Vault(it))
                        homeViewModel.setVaultSelection(HomeVaultSelection.Vault(it))
                    },
                    onTrashClick = {
                        scope.launch { drawerState.close() }
                        vaultDrawerViewModel.setVaultSelection(HomeVaultSelection.Trash)
                        homeViewModel.setVaultSelection(HomeVaultSelection.Trash)
                    },
                    onCreateVaultClick = { onNavigateEvent(HomeNavigation.CreateVault) },
                    onVaultOptionsClick = { share ->
                        currentBottomSheet = VaultOptions
                        selectedShare = share
                        scope.launch {
                            bottomSheetState.show()
                        }
                    }
                )
            }
        ) {
            HomeContent(
                modifier = Modifier.background(PassTheme.colors.backgroundStrong),
                uiState = homeUiState,
                shouldScrollToTop = homeUiState.homeListUiState.shouldScrollToTop,
                onItemClick = { item ->
                    homeViewModel.onItemClicked(item.shareId, item.id)
                    onNavigateEvent(HomeNavigation.ItemDetail(item.shareId, item.id))
                },
                onSearchQueryChange = { homeViewModel.onSearchQueryChange(it) },
                onEnterSearch = { homeViewModel.onEnterSearch() },
                onStopSearch = { homeViewModel.onStopSearching() },
                onDrawerIconClick = { scope.launch { drawerState.open() } },
                onSortingOptionsClick = {
                    onNavigateEvent(SortingBottomsheet(homeUiState.homeListUiState.sortingType))
                },
                onAddItemClick = { shareId: Option<ShareId>, itemTypeUiState: ItemTypeUiState ->
                    onNavigateEvent(HomeNavigation.AddItem(shareId, itemTypeUiState))
                },
                onItemMenuClick = { item ->
                    selectedItem = item
                    if (isTrashMode) {
                        currentBottomSheet = TrashItemOptions
                    } else {
                        when (item.itemType) {
                            is ItemType.Alias -> currentBottomSheet = AliasOptions
                            is ItemType.Login -> currentBottomSheet = LoginOptions
                            is ItemType.Note -> currentBottomSheet = NoteOptions
                            ItemType.Password -> {}
                        }
                    }
                    scope.launch { bottomSheetState.show() }
                },
                onRefresh = { homeViewModel.onRefresh() },
                onScrollToTop = { homeViewModel.onScrollToTop() },
                onProfileClick = { onNavigateEvent(HomeNavigation.Profile) },
                onItemTypeSelected = { homeViewModel.setItemTypeSelection(it) },
                onTrashActionsClick = {
                    currentBottomSheet = TrashOptions
                    scope.launch { bottomSheetState.show() }
                },
                onClearRecentSearchClick = homeViewModel::onClearAllRecentSearch
            )

            ConfirmRestoreAllDialog(
                show = shouldShowRestoreAllDialog,
                isLoading = actionState == ActionState.Loading,
                onDismiss = {
                    shouldShowRestoreAllDialog = false
                },
                onConfirm = {
                    homeViewModel.restoreItems()
                }
            )

            ConfirmClearTrashDialog(
                show = shouldShowClearTrashDialog,
                isLoading = actionState == ActionState.Loading,
                onDismiss = {
                    shouldShowClearTrashDialog = false
                },
                onConfirm = {
                    homeViewModel.clearTrash()
                }
            )

            ConfirmDeleteItemDialog(
                isLoading = actionState == ActionState.Loading,
                show = shouldShowDeleteItemDialog,
                onConfirm = {
                    val item = selectedItem ?: return@ConfirmDeleteItemDialog
                    homeViewModel.deleteItem(item.shareId, item.id, item.itemType)
                },
                onDismiss = { shouldShowDeleteItemDialog = false }
            )
        }
    }
}

object HomeScreenTestTag {
    const val screen = "homeScreen"
}
