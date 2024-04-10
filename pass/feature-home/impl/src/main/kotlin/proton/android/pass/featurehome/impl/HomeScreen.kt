/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.featurehome.impl

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import proton.android.pass.featurehome.impl.HomeBottomSheetType.AliasOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.CreditCardOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.LoginOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.Unknown
import proton.android.pass.featurehome.impl.HomeBottomSheetType.NoteOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.TrashItemOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.TrashOptions
import proton.android.pass.featurehome.impl.HomeNavigation.SortingBottomsheet
import proton.android.pass.featurehome.impl.bottomsheet.AliasOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.CreditCardOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.LoginOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.NoteOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.TrashAllBottomSheetContents
import proton.android.pass.featurehome.impl.needsupdate.AppNeedsUpdateBanner
import proton.android.pass.featurehome.impl.onboardingtips.NotificationPermissionLaunchedEffect
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTips
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipsEvent
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipsViewModel
import proton.android.pass.featurehome.impl.saver.HomeBottomSheetTypeSaver
import proton.android.pass.featurehome.impl.trash.ConfirmClearTrashDialog
import proton.android.pass.featurehome.impl.trash.ConfirmDeleteItemsDialog
import proton.android.pass.featurehome.impl.trash.ConfirmRestoreAllDialog
import proton.android.pass.featurehome.impl.trash.ConfirmRestoreItemsDialog
import proton.android.pass.featurehome.impl.trash.ConfirmTrashItemsDialog
import proton.android.pass.featurehome.impl.vault.VaultDrawerContent
import proton.android.pass.featurehome.impl.vault.VaultDrawerViewModel
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import proton.android.pass.featuretrash.impl.ConfirmDeleteItemDialog
import proton.android.pass.featuretrash.impl.ConfirmTrashAliasDialog
import proton.android.pass.featuretrash.impl.TrashItemBottomSheetContents

@OptIn(
    ExperimentalMaterialApi::class,
    ExperimentalComposeUiApi::class
)
@Suppress("ComplexMethod")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateEvent: (HomeNavigation) -> Unit,
    goToVault: ShareId? = null,
    enableBulkActions: Boolean = false,
    homeViewModel: HomeViewModel = hiltViewModel(),
    routerViewModel: RouterViewModel = hiltViewModel(),
    vaultDrawerViewModel: VaultDrawerViewModel = hiltViewModel(),
    onBoardingTipsViewModel: OnBoardingTipsViewModel = hiltViewModel()
) {
    val routerEvent by routerViewModel.eventStateFlow.collectAsStateWithLifecycle()
    val homeUiState by homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val drawerUiState by vaultDrawerViewModel.drawerUiState.collectAsStateWithLifecycle()
    val onBoardingTipsUiState by onBoardingTipsViewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(homeUiState.navEvent) {
        when (homeUiState.navEvent) {
            is HomeNavEvent.ItemHistory -> {
                (homeUiState.navEvent as HomeNavEvent.ItemHistory)
                    .let { event -> HomeNavigation.ItemHistory(event.shareId, event.itemId) }
                    .also(onNavigateEvent)
            }

            HomeNavEvent.ShowBulkMoveToVault -> {
                onNavigateEvent(HomeNavigation.MoveToVault)
            }

            HomeNavEvent.UpgradeDialog -> {
                onNavigateEvent(HomeNavigation.UpgradeDialog)
            }

            HomeNavEvent.Unknown -> {}
        }

        homeViewModel.onNavEventConsumed(homeUiState.navEvent)
    }

    var currentBottomSheet by rememberSaveable(stateSaver = HomeBottomSheetTypeSaver) {
        mutableStateOf(Unknown)
    }
    var selectedItem by rememberSaveable(stateSaver = ItemUiModelSaver) {
        mutableStateOf(null)
    }
    var shouldShowDeleteItemDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowRestoreAllDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowClearTrashDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowRestoreItemsDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowMoveToTrashItemsDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowDeleteItemsDialog by rememberSaveable { mutableStateOf(false) }
    var aliasToBeTrashed by rememberSaveable(stateSaver = ItemUiModelSaver) { mutableStateOf(null) }
    val scrollableState = rememberLazyListState()

    LaunchedEffect(enableBulkActions) {
        if (enableBulkActions) {
            homeViewModel.onBulkEnabled()
        }
    }

    LaunchedEffect(goToVault) {
        if (goToVault != null) {
            homeViewModel.setVaultSelection(VaultSelectionOption.Vault(goToVault))
        }
    }

    val actionState = homeUiState.homeListUiState.actionState
    LaunchedEffect(actionState) {
        if (actionState == ActionState.Done) {
            shouldShowDeleteItemDialog = false
            shouldShowRestoreAllDialog = false
            shouldShowClearTrashDialog = false
            shouldShowRestoreItemsDialog = false
            shouldShowMoveToTrashItemsDialog = false
            shouldShowDeleteItemsDialog = false
            homeViewModel.restoreActionState()
        }
    }

    LaunchedEffect(routerEvent) {
        when (routerEvent) {
            RouterEvent.OnBoarding -> onNavigateEvent(HomeNavigation.OnBoarding)
            RouterEvent.SyncDialog -> onNavigateEvent(HomeNavigation.SyncDialog)
            RouterEvent.ConfirmedInvite -> onNavigateEvent(HomeNavigation.ConfirmedInvite)
            RouterEvent.None -> {}
        }
    }

    LaunchedEffect(onBoardingTipsUiState.tipsToShow.hashCode()) {
        if (onBoardingTipsUiState.tipsToShow.isNotEmpty() && scrollableState.firstVisibleItemIndex == 0) {
            homeViewModel.scrollToTop()
        }
    }

    LaunchedEffect(onBoardingTipsUiState.event) {
        val homeNavigationEvent = when (onBoardingTipsUiState.event) {
            OnBoardingTipsEvent.OpenTrialScreen -> HomeNavigation.TrialInfo
            OnBoardingTipsEvent.OpenInviteScreen -> HomeNavigation.OpenInvite
            OnBoardingTipsEvent.RequestNotificationPermission,
            OnBoardingTipsEvent.Unknown -> return@LaunchedEffect
        }

        onNavigateEvent(homeNavigationEvent)
        onBoardingTipsViewModel.clearEvent()
    }

    NotificationPermissionLaunchedEffect(
        shouldRequestPermissions = onBoardingTipsUiState.event == OnBoardingTipsEvent.RequestNotificationPermission,
        onPermissionRequested = onBoardingTipsViewModel::clearEvent,
        onPermissionChanged = onBoardingTipsViewModel::onNotificationPermissionChanged
    )

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    LaunchedEffect(homeUiState.action) {
        when (homeUiState.action) {
            BottomSheetItemAction.None -> bottomSheetState.hide()
            BottomSheetItemAction.Pin,
            BottomSheetItemAction.Unpin,
            BottomSheetItemAction.History -> return@LaunchedEffect
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val isTrashMode = homeUiState.homeListUiState.homeVaultSelection == VaultSelectionOption.Trash

    val enableBackHandler = drawerState.isOpen ||
        bottomSheetState.isVisible ||
        homeUiState.homeListUiState.selectionState.isInSelectMode

    BackHandler(enableBackHandler) {
        scope.launch {
            when {
                drawerState.isOpen -> drawerState.close()
                bottomSheetState.isVisible -> bottomSheetState.hide()
                homeUiState.homeListUiState.selectionState.isInSelectMode ->
                    homeViewModel.clearSelection()
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
                    canLoadExternalImages = homeUiState.homeListUiState.canLoadExternalImages,
                    action = homeUiState.action,
                    onCopyUsername = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.Username)
                        }
                    },
                    onCopyPassword = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.Password)
                        }
                    },
                    onPinned = remember {
                        { shareId, itemId ->
                            homeViewModel.pinItem(shareId, itemId)
                        }
                    },
                    onUnpinned = remember {
                        { shareId, itemId ->
                            homeViewModel.unpinItem(shareId, itemId)
                        }
                    },
                    onViewHistory = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.viewItemHistory(shareId, itemId)
                        }
                    },
                    onEdit = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            onNavigateEvent(HomeNavigation.EditLogin(shareId, itemId))
                        }
                    },
                    onMoveToTrash = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.sendItemsToTrash(listOf(it))
                            }
                        }
                    },
                    onRemoveFromRecentSearch = remember {
                        { shareId, itemId ->
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.onClearRecentSearch(shareId, itemId)
                            }
                        }
                    },
                    isPinningFeatureEnabled = homeUiState.isPinningFeatureEnabled,
                    isHistoryFeatureEnabled = homeUiState.isHistoryFeatureEnabled,
                    isFreePlan = homeUiState.isFreePlan
                )

                AliasOptions -> AliasOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                    action = homeUiState.action,
                    onCopyAlias = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.Alias)
                        }
                    },
                    onPinned = remember {
                        { shareId, itemId ->
                            homeViewModel.pinItem(shareId, itemId)
                        }
                    },
                    onUnpinned = remember {
                        { shareId, itemId ->
                            homeViewModel.unpinItem(shareId, itemId)
                        }
                    },
                    onViewHistory = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.viewItemHistory(shareId, itemId)
                        }
                    },
                    onEdit = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            onNavigateEvent(HomeNavigation.EditAlias(shareId, itemId))
                        }
                    },
                    onMoveToTrash = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                aliasToBeTrashed = it
                            }
                        }
                    },
                    onRemoveFromRecentSearch = remember {
                        { shareId, itemId ->
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.onClearRecentSearch(shareId, itemId)
                            }
                        }
                    },
                    isPinningFeatureEnabled = homeUiState.isPinningFeatureEnabled,
                    isHistoryFeatureEnabled = homeUiState.isHistoryFeatureEnabled,
                    isFreePlan = homeUiState.isFreePlan
                )

                NoteOptions -> NoteOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                    action = homeUiState.action,
                    onCopyNote = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.Note)
                        }
                    },
                    onPinned = remember {
                        { shareId, itemId ->
                            homeViewModel.pinItem(shareId, itemId)
                        }
                    },
                    onUnpinned = remember {
                        { shareId, itemId ->
                            homeViewModel.unpinItem(shareId, itemId)
                        }
                    },
                    onViewHistory = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.viewItemHistory(shareId, itemId)
                        }
                    },
                    onEdit = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            onNavigateEvent(HomeNavigation.EditNote(shareId, itemId))
                        }
                    },
                    onMoveToTrash = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.sendItemsToTrash(listOf(it))
                            }
                        }
                    },
                    onRemoveFromRecentSearch = remember {
                        { shareId, itemId ->
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.onClearRecentSearch(shareId, itemId)
                            }
                        }
                    },
                    isPinningFeatureEnabled = homeUiState.isPinningFeatureEnabled,
                    isHistoryFeatureEnabled = homeUiState.isHistoryFeatureEnabled,
                    isFreePlan = homeUiState.isFreePlan
                )

                CreditCardOptions -> CreditCardOptionsBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                    action = homeUiState.action,
                    onCopyNumber = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.CreditCardNumber)
                        }
                    },
                    onCopyCvv = remember {
                        {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.CreditCardCvv)
                        }
                    },
                    onPinned = remember {
                        { shareId, itemId ->
                            homeViewModel.pinItem(shareId, itemId)
                        }
                    },
                    onUnpinned = remember {
                        { shareId, itemId ->
                            homeViewModel.unpinItem(shareId, itemId)
                        }
                    },
                    onViewHistory = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.viewItemHistory(shareId, itemId)
                        }
                    },
                    onEdit = remember {
                        { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            onNavigateEvent(HomeNavigation.EditCreditCard(shareId, itemId))
                        }
                    },
                    onMoveToTrash = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.sendItemsToTrash(listOf(it))
                            }
                        }
                    },
                    onRemoveFromRecentSearch = remember {
                        { shareId, itemId ->
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.onClearRecentSearch(shareId, itemId)
                            }
                        }
                    },
                    isPinningFeatureEnabled = homeUiState.isPinningFeatureEnabled,
                    isHistoryFeatureEnabled = homeUiState.isHistoryFeatureEnabled,
                    isFreePlan = homeUiState.isFreePlan
                )

                TrashItemOptions -> TrashItemBottomSheetContents(
                    itemUiModel = selectedItem!!,
                    onRestoreItem = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                homeViewModel.restoreItems(listOf(it))
                            }
                        }
                    },
                    onDeleteItem = remember {
                        {
                            scope.launch {
                                bottomSheetState.hide()
                                shouldShowDeleteItemDialog = true
                            }
                        }
                    },
                    icon = {
                        when (val contents = selectedItem!!.contents) {
                            is ItemContents.Login -> {
                                val sortedPackages =
                                    contents.packageInfoSet.sortedBy { it.packageName.value }
                                val packageName = sortedPackages.firstOrNull()?.packageName?.value
                                val website = contents.urls.firstOrNull()
                                LoginIcon(
                                    text = selectedItem!!.contents.title,
                                    canLoadExternalImages = homeUiState.homeListUiState.canLoadExternalImages,
                                    website = website,
                                    packageName = packageName
                                )
                            }

                            is ItemContents.Alias -> AliasIcon()
                            is ItemContents.Note -> NoteIcon()
                            is ItemContents.CreditCard -> CreditCardIcon()
                            is ItemContents.Unknown -> {}
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
                    },
                    onSelectItems = {
                        scope.launch {
                            bottomSheetState.hide()
                        }
                        homeViewModel.onBulkEnabled()
                    }
                )

                Unknown -> {}
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
                    canCreateVault = drawerUiState.canCreateVault,
                    onAllVaultsClick = remember {
                        {
                            scope.launch { drawerState.close() }
                            vaultDrawerViewModel.setVaultSelection(VaultSelectionOption.AllVaults)
                            homeViewModel.setVaultSelection(VaultSelectionOption.AllVaults)
                        }
                    },
                    onVaultClick = remember {
                        {
                            scope.launch { drawerState.close() }
                            vaultDrawerViewModel.setVaultSelection(VaultSelectionOption.Vault(it))
                            homeViewModel.setVaultSelection(VaultSelectionOption.Vault(it))
                        }
                    },
                    onTrashClick = remember {
                        {
                            scope.launch { drawerState.close() }
                            vaultDrawerViewModel.setVaultSelection(VaultSelectionOption.Trash)
                            homeViewModel.setVaultSelection(VaultSelectionOption.Trash)
                        }
                    },
                    onCreateVaultClick = remember { { onNavigateEvent(HomeNavigation.CreateVault) } },
                    onVaultOptionsClick = remember {
                        {
                            onNavigateEvent(
                                HomeNavigation.VaultOptions(
                                    it.id
                                )
                            )
                        }
                    }
                )
            }
        ) {
            HomeContent(
                modifier = Modifier.background(PassTheme.colors.backgroundStrong),
                uiState = homeUiState,
                scrollableState = scrollableState,
                shouldScrollToTop = homeUiState.homeListUiState.shouldScrollToTop,
                header = {
                    if (homeUiState.homeListUiState.showNeedsUpdate) {
                        item("needsUpdate") {
                            val context = LocalContext.current
                            Column {
                                AppNeedsUpdateBanner(
                                    onClick = {
                                        homeViewModel.openUpdateApp(context.toClassHolder())
                                    }
                                )
                                Spacer(modifier = Modifier.height(Spacing.small))
                            }
                        }
                    }
                    item("header") {
                        OnBoardingTips(
                            onClick = onBoardingTipsViewModel::onClick,
                            onDismiss = onBoardingTipsViewModel::onDismiss,
                            state = onBoardingTipsUiState
                        )
                    }
                },
                onEvent = { homeUiEvent ->
                    when (homeUiEvent) {
                        is HomeUiEvent.ItemClick -> {
                            homeViewModel.onItemClicked(
                                homeUiEvent.item.shareId,
                                homeUiEvent.item.id
                            )
                            onNavigateEvent(
                                HomeNavigation.ItemDetail(
                                    homeUiEvent.item.shareId,
                                    homeUiEvent.item.id
                                )
                            )
                        }

                        is HomeUiEvent.SearchQueryChange -> {
                            homeViewModel.onSearchQueryChange(homeUiEvent.query)
                        }

                        is HomeUiEvent.EnterSearch -> {
                            homeViewModel.onEnterSearch()
                        }

                        is HomeUiEvent.StopSearch -> {
                            homeViewModel.onStopSearching()
                        }

                        is HomeUiEvent.DrawerIconClick -> {
                            scope.launch { drawerState.open() }
                        }

                        is HomeUiEvent.SortingOptionsClick -> {
                            onNavigateEvent(SortingBottomsheet)
                        }

                        is HomeUiEvent.AddItemClick -> {
                            onNavigateEvent(
                                HomeNavigation.AddItem(
                                    homeUiEvent.shareId,
                                    homeUiEvent.state
                                )
                            )
                        }

                        is HomeUiEvent.ItemMenuClick -> {
                            selectedItem = homeUiEvent.item
                            currentBottomSheet = if (isTrashMode || homeUiEvent.item.isInTrash()) {
                                TrashItemOptions
                            } else {
                                when (homeUiEvent.item.contents) {
                                    is ItemContents.Alias -> AliasOptions
                                    is ItemContents.Login -> LoginOptions
                                    is ItemContents.Note -> NoteOptions
                                    is ItemContents.CreditCard -> CreditCardOptions
                                    is ItemContents.Unknown -> LoginOptions
                                }
                            }
                            scope.launch { bottomSheetState.show() }
                        }

                        is HomeUiEvent.Refresh -> {
                            homeViewModel.onRefresh()
                        }

                        is HomeUiEvent.ScrollToTop -> {
                            homeViewModel.onScrollToTop()
                        }

                        is HomeUiEvent.ProfileClick -> {
                            onNavigateEvent(HomeNavigation.Profile)
                        }

                        is HomeUiEvent.ItemTypeSelected -> {
                            homeViewModel.setItemTypeSelection(
                                searchFilterType = homeUiEvent.searchFilterType
                            )
                        }

                        is HomeUiEvent.ActionsClick -> {
                            if (isTrashMode) {
                                currentBottomSheet = TrashOptions
                                scope.launch { bottomSheetState.show() }
                            } else {
                                val readOnly = homeUiState.isSelectedVaultReadOnly()
                                val inSearchMode = homeUiState.searchUiState.inSearchMode
                                onNavigateEvent(HomeNavigation.SearchOptions(!readOnly && !inSearchMode))
                            }
                        }

                        is HomeUiEvent.ClearRecentSearchClick -> {
                            homeViewModel.onClearAllRecentSearch()
                        }

                        is HomeUiEvent.SelectItem -> {
                            if (homeUiEvent.item.canModify) {
                                homeViewModel.onItemSelected(homeUiEvent.item)
                            } else {
                                homeViewModel.onReadOnlyItemSelected()
                            }
                        }

                        HomeUiEvent.MoveToTrashItemsActionClick -> {
                            shouldShowMoveToTrashItemsDialog = true
                        }

                        is HomeUiEvent.StopBulk -> {
                            homeViewModel.clearSelection()
                        }


                        HomeUiEvent.PermanentlyDeleteItemsActionClick -> {
                            shouldShowDeleteItemsDialog = true
                        }

                        HomeUiEvent.RestoreItemsActionClick -> {
                            shouldShowRestoreItemsDialog = true
                        }

                        HomeUiEvent.MoveItemsActionClick -> {
                            homeViewModel.moveItemsToVault(
                                items = homeUiState.homeListUiState.selectionState.selectedItems
                            )
                        }

                        HomeUiEvent.SeeAllPinned -> {
                            homeViewModel.onSeeAllPinned()
                        }

                        HomeUiEvent.PinItemsActionClick -> {
                            homeViewModel.pinSelectedItems(
                                items = homeUiState.homeListUiState.selectionState.selectedItems
                            )
                        }

                        HomeUiEvent.UnpinItemsActionClick -> {
                            homeViewModel.unpinSelectedItems(
                                items = homeUiState.homeListUiState.selectionState.selectedItems
                            )
                        }

                        HomeUiEvent.StopSeeAllPinned -> {
                            homeViewModel.onStopSeeAllPinned()
                        }

                        HomeUiEvent.SecurityCenterClick -> {
                            onNavigateEvent(HomeNavigation.SecurityCenter)
                        }
                    }
                }
            )

            ConfirmRestoreAllDialog(
                show = shouldShowRestoreAllDialog,
                isLoading = actionState == ActionState.Loading,
                onDismiss = {
                    shouldShowRestoreAllDialog = false
                },
                onConfirm = {
                    homeViewModel.restoreAllItems()
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
                    selectedItem?.let {
                        homeViewModel.deleteItems(listOf(it))
                    }
                },
                onDismiss = { shouldShowDeleteItemDialog = false }
            )

            ConfirmTrashAliasDialog(
                show = aliasToBeTrashed != null,
                onConfirm = {
                    val item = aliasToBeTrashed ?: return@ConfirmTrashAliasDialog
                    homeViewModel.sendItemsToTrash(listOf(item))
                    aliasToBeTrashed = null
                },
                onDismiss = { aliasToBeTrashed = null }
            )

            ConfirmRestoreItemsDialog(
                show = shouldShowRestoreItemsDialog,
                isLoading = actionState == ActionState.Loading,
                amount = homeUiState.homeListUiState.selectionState.selectedItems.size,
                onConfirm = {
                    homeViewModel.restoreItems(
                        homeUiState.homeListUiState.selectionState.selectedItems
                    )
                },
                onDismiss = { shouldShowRestoreItemsDialog = false }
            )

            ConfirmTrashItemsDialog(
                show = shouldShowMoveToTrashItemsDialog,
                isLoading = actionState == ActionState.Loading,
                amount = homeUiState.homeListUiState.selectionState.selectedItems.size,
                onConfirm = {
                    homeViewModel.sendItemsToTrash(
                        homeUiState.homeListUiState.selectionState.selectedItems
                    )
                },
                onDismiss = { shouldShowMoveToTrashItemsDialog = false }
            )

            ConfirmDeleteItemsDialog(
                show = shouldShowDeleteItemsDialog,
                isLoading = actionState == ActionState.Loading,
                amount = homeUiState.homeListUiState.selectionState.selectedItems.size,
                onConfirm = {
                    homeViewModel.deleteItems(
                        homeUiState.homeListUiState.selectionState.selectedItems
                    )
                },
                onDismiss = { shouldShowDeleteItemsDialog = false }
            )
        }
    }
}

object HomeScreenTestTag {
    const val SCREEN = "homeScreen"
}
