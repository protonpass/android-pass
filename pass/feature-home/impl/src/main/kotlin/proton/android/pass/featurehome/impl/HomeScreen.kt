package proton.android.pass.featurehome.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.ModalDrawer
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import proton.android.pass.commonuimodels.api.ShareUiModelWithItemCount
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.dialogs.ConfirmDeleteItemDialog
import proton.android.pass.featurehome.impl.bottomsheet.AliasOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.LoginOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.NoteOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.SortingBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.TrashAllBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.TrashItemBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.VaultOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.saver.HomeBottomSheetTypeSaver
import proton.android.pass.featurehome.impl.trash.ConfirmClearTrashDialog
import proton.android.pass.featurehome.impl.trash.ConfirmRestoreAllDialog
import proton.android.pass.featurehome.impl.vault.VaultDeleteDialog
import proton.android.pass.featurehome.impl.vault.VaultDrawerContent
import proton.android.pass.featurehome.impl.vault.VaultDrawerViewModel
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
    homeScreenNavigation: HomeScreenNavigation,
    onAddItemClick: (Option<ShareId>) -> Unit,
    onCreateVaultClick: () -> Unit,
    onEditVaultClick: (ShareId) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
    vaultDrawerViewModel: VaultDrawerViewModel = hiltViewModel()
) {
    val homeUiState by homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val drawerUiState by vaultDrawerViewModel.drawerUiState.collectAsStateWithLifecycle()

    var currentBottomSheet by rememberSaveable(stateSaver = HomeBottomSheetTypeSaver) {
        mutableStateOf(HomeBottomSheetType.Sorting)
    }
    var selectedItem by rememberSaveable(stateSaver = ItemUiModelSaver) {
        mutableStateOf(null)
    }
    var shouldScrollToTop by remember { mutableStateOf(false) }
    var shouldShowDeleteItemDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowDeleteVaultDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowRestoreAllDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowClearTrashDialog by rememberSaveable { mutableStateOf(false) }
    var selectedShare: ShareUiModelWithItemCount? by rememberSaveable(stateSaver = ShareUiModelWithItemCountSaver) {
        mutableStateOf(null)
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
                HomeBottomSheetType.Sorting -> SortingBottomSheetContents(
                    sortingType = homeUiState.homeListUiState.sortingType
                ) {
                    homeViewModel.onSortingTypeChanged(it)
                    shouldScrollToTop = true
                    scope.launch { bottomSheetState.hide() }
                }
                HomeBottomSheetType.LoginOptions -> LoginOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
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
                        homeScreenNavigation.toEditLogin(shareId, itemId)
                    },
                    onMoveToTrash = {
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.sendItemToTrash(it)
                        }
                    }
                )
                HomeBottomSheetType.AliasOptions -> AliasOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    onCopyAlias = {
                        scope.launch { bottomSheetState.hide() }
                        homeViewModel.copyToClipboard(it, HomeClipboardType.Alias)
                    },
                    onEdit = { shareId, itemId ->
                        scope.launch { bottomSheetState.hide() }
                        homeScreenNavigation.toEditAlias(shareId, itemId)
                    },
                    onMoveToTrash = {
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.sendItemToTrash(it)
                        }
                    }
                )
                HomeBottomSheetType.NoteOptions -> NoteOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    onCopyNote = {
                        scope.launch { bottomSheetState.hide() }
                        homeViewModel.copyToClipboard(it, HomeClipboardType.Note)
                    },
                    onEdit = { shareId, itemId ->
                        scope.launch { bottomSheetState.hide() }
                        homeScreenNavigation.toEditNote(shareId, itemId)
                    },
                    onMoveToTrash = {
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.sendItemToTrash(it)
                        }
                    }
                )
                HomeBottomSheetType.VaultOptions -> {
                    val showDelete = when (val share = homeUiState.homeListUiState.selectedShare) {
                        None -> true
                        is Some -> share.value.id.id != selectedShare?.id?.id
                    }
                    VaultOptionsBottomSheetContents(
                        showDelete = showDelete,
                        onEdit = {
                            scope.launch {
                                bottomSheetState.hide()
                                selectedShare?.let { onEditVaultClick(it.id) }
                            }
                        },
                        onRemove = {
                            scope.launch {
                                bottomSheetState.hide()
                                shouldShowDeleteVaultDialog = true
                            }
                        }
                    )
                }
                HomeBottomSheetType.TrashItemOptions -> TrashItemBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    onRestoreItem = {
                        scope.launch {
                            bottomSheetState.hide()
                            homeViewModel.restoreItem(it)
                        }
                    },
                    onDeleteItem = {
                        scope.launch {
                            bottomSheetState.hide()
                            shouldShowDeleteItemDialog = true
                        }
                    }
                )
                HomeBottomSheetType.TrashOptions -> TrashAllBottomSheetContents(
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
                    onCreateVaultClick = {
                        onCreateVaultClick()
                    },
                    onVaultOptionsClick = { share ->
                        currentBottomSheet = HomeBottomSheetType.VaultOptions
                        selectedShare = share
                        scope.launch {
                            bottomSheetState.show()
                        }
                    }
                )
            }
        ) {
            HomeContent(
                uiState = homeUiState,
                shouldScrollToTop = shouldScrollToTop,
                homeScreenNavigation = homeScreenNavigation,
                onSearchQueryChange = { homeViewModel.onSearchQueryChange(it) },
                onEnterSearch = { homeViewModel.onEnterSearch() },
                onStopSearch = { homeViewModel.onStopSearching() },
                sendItemToTrash = { homeViewModel.sendItemToTrash(it) },
                onDrawerIconClick = { scope.launch { drawerState.open() } },
                onSortingOptionsClick = {
                    currentBottomSheet = HomeBottomSheetType.Sorting
                    scope.launch { bottomSheetState.show() }
                },
                onAddItemClick = onAddItemClick,
                onItemMenuClick = { item ->
                    selectedItem = item
                    if (isTrashMode) {
                        currentBottomSheet = HomeBottomSheetType.TrashItemOptions
                    } else {
                        when (item.itemType) {
                            is ItemType.Alias -> currentBottomSheet = HomeBottomSheetType.AliasOptions
                            is ItemType.Login -> currentBottomSheet = HomeBottomSheetType.LoginOptions
                            is ItemType.Note -> currentBottomSheet = HomeBottomSheetType.NoteOptions
                            ItemType.Password -> {}
                        }
                    }
                    scope.launch { bottomSheetState.show() }
                },
                onRefresh = { homeViewModel.onRefresh() },
                onScrollToTop = { shouldScrollToTop = false },
                onProfileClick = { homeScreenNavigation.toProfile() },
                onItemTypeSelected = { homeViewModel.setItemTypeSelection(it) },
                onTrashActionsClick = {
                    currentBottomSheet = HomeBottomSheetType.TrashOptions
                    scope.launch { bottomSheetState.show() }
                }
            )

            VaultDeleteDialog(
                show = shouldShowDeleteVaultDialog,
                vaultName = selectedShare?.name,
                onDismiss = {
                    shouldShowDeleteVaultDialog = false
                    selectedShare = null
                },
                onDelete = {
                    scope.launch {
                        drawerState.close()
                        shouldShowDeleteVaultDialog = false

                        selectedShare?.let { share ->
                            vaultDrawerViewModel.onDeleteVault(share.id)
                        }
                    }
                },
                onCancel = {
                    shouldShowDeleteVaultDialog = false
                    selectedShare = null
                }
            )

            ConfirmRestoreAllDialog(
                show = shouldShowRestoreAllDialog,
                onDismiss = {
                    shouldShowRestoreAllDialog = false
                },
                onConfirm = {
                    homeViewModel.restoreItems()
                    shouldShowRestoreAllDialog = false
                }
            )

            ConfirmClearTrashDialog(
                show = shouldShowClearTrashDialog,
                onDismiss = {
                    shouldShowClearTrashDialog = false
                },
                onConfirm = {
                    homeViewModel.clearTrash()
                    shouldShowClearTrashDialog = false
                }
            )

            ConfirmDeleteItemDialog(
                itemName = selectedItem?.name ?: "",
                show = shouldShowDeleteItemDialog,
                onConfirm = {
                    homeViewModel.deleteItem(selectedItem!!)
                    shouldShowDeleteItemDialog = false
                },
                onDismiss = { shouldShowDeleteItemDialog = false },
                onCancel = { shouldShowDeleteItemDialog = false }
            )
        }
    }
}
