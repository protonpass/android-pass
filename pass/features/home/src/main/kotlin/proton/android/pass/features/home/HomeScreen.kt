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

package proton.android.pass.features.home

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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.CustomIcon
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.domain.ItemContents
import proton.android.pass.features.home.HomeBottomSheetType.AliasOptions
import proton.android.pass.features.home.HomeBottomSheetType.CreditCardOptions
import proton.android.pass.features.home.HomeBottomSheetType.CustomOptions
import proton.android.pass.features.home.HomeBottomSheetType.IdentityOptions
import proton.android.pass.features.home.HomeBottomSheetType.LoginOptions
import proton.android.pass.features.home.HomeBottomSheetType.NoteOptions
import proton.android.pass.features.home.HomeBottomSheetType.TrashItemOptions
import proton.android.pass.features.home.HomeBottomSheetType.TrashOptions
import proton.android.pass.features.home.HomeBottomSheetType.Unknown
import proton.android.pass.features.home.HomeNavigation.SortingBottomsheet
import proton.android.pass.features.home.bottomsheet.AliasOptionsBottomSheetContents
import proton.android.pass.features.home.bottomsheet.CreditCardOptionsBottomSheetContents
import proton.android.pass.features.home.bottomsheet.CustomItemOptionsBottomSheetContents
import proton.android.pass.features.home.bottomsheet.IdentityOptionsBottomSheetContents
import proton.android.pass.features.home.bottomsheet.LoginOptionsBottomSheetContents
import proton.android.pass.features.home.bottomsheet.NoteOptionsBottomSheetContents
import proton.android.pass.features.home.bottomsheet.TrashAllBottomSheetContents
import proton.android.pass.features.home.drawer.presentation.HomeDrawerViewModel
import proton.android.pass.features.home.drawer.ui.HomeDrawerContent
import proton.android.pass.features.home.drawer.ui.HomeDrawerUiEvent
import proton.android.pass.features.home.needsupdate.AppNeedsUpdateBanner
import proton.android.pass.features.home.onboardingtips.NotificationPermissionLaunchedEffect
import proton.android.pass.features.home.onboardingtips.OnBoardingTips
import proton.android.pass.features.home.onboardingtips.OnBoardingTipsEvent
import proton.android.pass.features.home.onboardingtips.OnBoardingTipsViewModel
import proton.android.pass.features.home.saver.HomeBottomSheetTypeSaver
import proton.android.pass.features.home.saver.ItemUiModelSaver
import proton.android.pass.features.home.trash.ConfirmClearTrashDialog
import proton.android.pass.features.home.trash.ConfirmDeleteItemsDialog
import proton.android.pass.features.home.trash.ConfirmRestoreAllDialog
import proton.android.pass.features.home.trash.ConfirmRestoreItemsDialog
import proton.android.pass.features.home.trash.ConfirmTrashItemsDialog
import proton.android.pass.features.home.vault.VaultDrawerContent
import proton.android.pass.features.home.vault.VaultDrawerViewModel
import proton.android.pass.features.trash.ConfirmBulkDeleteAliasDialog
import proton.android.pass.features.trash.ConfirmDeleteDisabledAliasDialog
import proton.android.pass.features.trash.ConfirmDeleteEnabledAliasDialog
import proton.android.pass.features.trash.ConfirmDeleteItemDialog
import proton.android.pass.features.trash.ConfirmTrashAliasDialog
import proton.android.pass.features.trash.TrashItemBottomSheetContents
import proton.android.pass.searchoptions.api.VaultSelectionOption

@OptIn(ExperimentalMaterialApi::class)
@Suppress("ComplexMethod")
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateEvent: (HomeNavigation) -> Unit,
    enableBulkActions: Boolean = false,
    homeViewModel: HomeViewModel = hiltViewModel(),
    routerViewModel: RouterViewModel = hiltViewModel(),
    vaultDrawerViewModel: VaultDrawerViewModel = hiltViewModel(),
    onBoardingTipsViewModel: OnBoardingTipsViewModel = hiltViewModel(),
    homeDrawerViewModel: HomeDrawerViewModel = hiltViewModel()
) {
    val routerEvent by routerViewModel.routerEventState.collectAsStateWithLifecycle(RouterEvent.None)
    val homeUiState by homeViewModel.homeUiState.collectAsStateWithLifecycle()
    val onBoardingTipsUiState by onBoardingTipsViewModel.stateFlow.collectAsStateWithLifecycle()

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

            HomeNavEvent.Unknown -> Unit

            HomeNavEvent.OnBulkMigrationSharedWarning -> {
                onNavigateEvent(HomeNavigation.ItemsMigrationSharedWarning)
            }
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
    var shouldShowMoveToTrashAliasDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowDeleteEnabledAliasDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowDeleteDisabledAliasDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowBulkDeleteAliasDialog by rememberSaveable { mutableStateOf(false) }
    val scrollableState = rememberLazyListState()

    DisposableEffect(Unit) {
        onDispose {
            homeViewModel.clearSelection()
        }
    }

    LaunchedEffect(enableBulkActions) {
        if (enableBulkActions) {
            homeViewModel.onBulkEnabled()
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
            shouldShowMoveToTrashAliasDialog = false
            shouldShowDeleteEnabledAliasDialog = false
            shouldShowDeleteDisabledAliasDialog = false
            shouldShowBulkDeleteAliasDialog = false
            homeViewModel.restoreActionState()
        }
    }

    DisposableEffect(routerEvent) {
        when (val event = routerEvent) {
            RouterEvent.OnBoarding -> onNavigateEvent(HomeNavigation.OnBoarding)
            is RouterEvent.ConfirmedInvite -> HomeNavigation.ConfirmedInvite(
                inviteToken = event.inviteToken
            ).also(onNavigateEvent)

            RouterEvent.SyncDialog -> onNavigateEvent(HomeNavigation.SyncDialog)
            RouterEvent.None -> Unit
        }
        onDispose { routerViewModel.clearEvent() }
    }

    LaunchedEffect(onBoardingTipsUiState.tipToShow) {
        if (onBoardingTipsUiState.tipToShow is Some && scrollableState.firstVisibleItemIndex <= 1) {
            homeViewModel.scrollToTop()
        }
    }

    LaunchedEffect(onBoardingTipsUiState.event) {
        val homeNavigationEvent = when (val event = onBoardingTipsUiState.event) {
            OnBoardingTipsEvent.OpenTrialScreen -> HomeNavigation.TrialInfo
            is OnBoardingTipsEvent.OpenInviteScreen -> HomeNavigation.OpenInvite(event.inviteToken)
            is OnBoardingTipsEvent.OpenSLSyncSettingsScreen -> HomeNavigation.SLSyncSettings(event.shareId)
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
            BottomSheetItemAction.Migrate,
            BottomSheetItemAction.MonitorExclude,
            BottomSheetItemAction.MonitorInclude,
            BottomSheetItemAction.Pin,
            BottomSheetItemAction.Unpin,
            BottomSheetItemAction.History,
            BottomSheetItemAction.Remove,
            BottomSheetItemAction.Restore,
            BottomSheetItemAction.ResetHistory,
            BottomSheetItemAction.Trash -> return@LaunchedEffect
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
                LoginOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    LoginOptionsBottomSheetContents(
                        itemUiModel = item,
                        canUpdate = homeUiState.homeListUiState.checkCanUpdate(item.shareId),
                        canViewHistory = homeUiState.homeListUiState.canViewHistory(item.shareId),
                        isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                        canLoadExternalImages = homeUiState.homeListUiState.canLoadExternalImages,
                        action = homeUiState.action,
                        onCopyEmail = remember {
                            {
                                scope.launch { bottomSheetState.hide() }
                                homeViewModel.copyToClipboard(it, HomeClipboardType.Email)
                            }
                        },
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
                                scope.launch { bottomSheetState.hide() }
                                homeViewModel.sendItemsToTrash(listOf(it))
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
                        isFreePlan = homeUiState.isFreePlan
                    )
                }

                AliasOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    AliasOptionsBottomSheetContents(
                        itemUiModel = item,
                        canUpdate = homeUiState.homeListUiState.checkCanUpdate(item.shareId),
                        canViewHistory = homeUiState.homeListUiState.canViewHistory(item.shareId),
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
                            { itemUiModel ->
                                when {
                                    homeUiState.shouldDisplayTrashAliasDialog(itemUiModel) -> {
                                        scope.launch { bottomSheetState.hide() }

                                        HomeNavigation.TrashAlias(
                                            shareId = itemUiModel.shareId,
                                            itemId = itemUiModel.id
                                        ).also(onNavigateEvent)
                                    }

                                    homeUiState.isSLAliasSyncEnabled -> {
                                        homeViewModel.sendItemsToTrash(listOf(itemUiModel))
                                    }

                                    else -> {
                                        selectedItem = itemUiModel
                                        shouldShowMoveToTrashAliasDialog = true
                                    }
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
                        isFreePlan = homeUiState.isFreePlan
                    )
                }

                NoteOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    NoteOptionsBottomSheetContents(
                        itemUiModel = item,
                        canUpdate = homeUiState.homeListUiState.checkCanUpdate(item.shareId),
                        canViewHistory = homeUiState.homeListUiState.canViewHistory(item.shareId),
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
                                scope.launch { bottomSheetState.hide() }

                                homeViewModel.sendItemsToTrash(listOf(it))
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
                        isFreePlan = homeUiState.isFreePlan
                    )
                }

                CreditCardOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    CreditCardOptionsBottomSheetContents(
                        itemUiModel = item,
                        canUpdate = homeUiState.homeListUiState.checkCanUpdate(item.shareId),
                        canViewHistory = homeUiState.homeListUiState.canViewHistory(item.shareId),
                        isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                        action = homeUiState.action,
                        onCopyNumber = remember {
                            {
                                scope.launch { bottomSheetState.hide() }
                                homeViewModel.copyToClipboard(
                                    it,
                                    HomeClipboardType.CreditCardNumber
                                )
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
                                scope.launch { bottomSheetState.hide() }

                                homeViewModel.sendItemsToTrash(listOf(it))
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
                        isFreePlan = homeUiState.isFreePlan
                    )
                }

                IdentityOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    IdentityOptionsBottomSheetContents(
                        itemUiModel = item,
                        canUpdate = homeUiState.homeListUiState.checkCanUpdate(item.shareId),
                        canViewHistory = homeUiState.homeListUiState.canViewHistory(item.shareId),
                        isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                        action = homeUiState.action,
                        isFreePlan = homeUiState.isFreePlan,
                        onCopyFullName = {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.copyToClipboard(it, HomeClipboardType.FullName)
                        },
                        onPinned = { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.pinItem(shareId, itemId)
                        },
                        onUnpinned = { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.unpinItem(shareId, itemId)
                        },
                        onViewHistory = { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.viewItemHistory(shareId, itemId)
                        },
                        onEdit = { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            onNavigateEvent(HomeNavigation.EditIdentity(shareId, itemId))
                        },
                        onMoveToTrash = {
                            scope.launch { bottomSheetState.hide() }

                            homeViewModel.sendItemsToTrash(listOf(it))
                        },
                        onRemoveFromRecentSearch = { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.onClearRecentSearch(shareId, itemId)
                        }
                    )
                }

                CustomOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    CustomItemOptionsBottomSheetContents(
                        itemUiModel = item,
                        canUpdate = homeUiState.homeListUiState.checkCanUpdate(item.shareId),
                        canViewHistory = homeUiState.homeListUiState.canViewHistory(item.shareId),
                        isRecentSearch = homeUiState.searchUiState.isInSuggestionsMode,
                        action = homeUiState.action,
                        isFreePlan = homeUiState.isFreePlan,
                        onPinned = { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.pinItem(shareId, itemId)
                        },
                        onUnpinned = { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.unpinItem(shareId, itemId)
                        },
                        onViewHistory = { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.viewItemHistory(shareId, itemId)
                        },
                        onEdit = { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            onNavigateEvent(HomeNavigation.EditCustomItem(shareId, itemId))
                        },
                        onMoveToTrash = {
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.sendItemsToTrash(listOf(it))
                        },
                        onRemoveFromRecentSearch = { shareId, itemId ->
                            scope.launch { bottomSheetState.hide() }
                            homeViewModel.onClearRecentSearch(shareId, itemId)
                        }
                    )
                }

                TrashItemOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    TrashItemBottomSheetContents(
                        canBeDeleted = homeUiState.homeListUiState.canBeDeleted(item.shareId),
                        itemUiModel = item,
                        onLeaveItem = remember {
                            {
                                scope.launch { bottomSheetState.hide() }

                                HomeNavigation.LeaveItemShare(
                                    shareId = it.shareId
                                ).also(onNavigateEvent)
                            }
                        },
                        onRestoreItem = remember {
                            {
                                scope.launch {
                                    bottomSheetState.hide()
                                    homeViewModel.restoreItems(listOf(it))
                                }
                            }
                        },
                        onDeleteItem = {
                            val contents = it.contents
                            scope.launch {
                                bottomSheetState.hide()
                                if (contents is ItemContents.Alias) {
                                    selectedItem = it
                                    if (contents.isEnabled) {
                                        shouldShowDeleteEnabledAliasDialog = true
                                    } else {
                                        shouldShowDeleteDisabledAliasDialog = true
                                    }
                                } else {
                                    shouldShowDeleteItemDialog = true
                                }
                            }
                        },
                        icon = {
                            when (val contents = item.contents) {
                                is ItemContents.Login -> {
                                    val sortedPackages =
                                        contents.packageInfoSet.sortedBy { it.packageName.value }
                                    val packageName =
                                        sortedPackages.firstOrNull()?.packageName?.value
                                    val website = contents.urls.firstOrNull()
                                    LoginIcon(
                                        text = item.contents.title,
                                        canLoadExternalImages = homeUiState.homeListUiState.canLoadExternalImages,
                                        website = website,
                                        packageName = packageName
                                    )
                                }

                                is ItemContents.Alias -> AliasIcon(
                                    activeAlias = contents.isEnabled
                                )

                                is ItemContents.Note -> NoteIcon()
                                is ItemContents.CreditCard -> CreditCardIcon()
                                is ItemContents.Identity -> IdentityIcon()
                                is ItemContents.Custom -> CustomIcon()
                                is ItemContents.Unknown -> {}
                            }
                        }
                    )
                }

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
            gesturesEnabled = homeUiState.isDrawerAvailable,
            drawerContent = {
                if (homeUiState.isItemSharingEnabled) {
                    val homeDrawerState by homeDrawerViewModel.stateFlow.collectAsStateWithLifecycle()

                    HomeDrawerContent(
                        state = homeDrawerState,
                        onUiEvent = { uiEvent ->
                            when (uiEvent) {
                                HomeDrawerUiEvent.OnAllVaultsClick -> {
                                    scope.launch { drawerState.close() }

                                    VaultSelectionOption.AllVaults
                                        .also(homeDrawerViewModel::setVaultSelection)
                                        .also(homeViewModel::setVaultSelection)
                                }

                                is HomeDrawerUiEvent.OnVaultClick -> {
                                    scope.launch { drawerState.close() }

                                    VaultSelectionOption.Vault(shareId = uiEvent.shareId)
                                        .also(homeDrawerViewModel::setVaultSelection)
                                        .also(homeViewModel::setVaultSelection)
                                }

                                is HomeDrawerUiEvent.OnShareVaultClick -> {
                                    HomeNavigation.ShareVault(
                                        shareId = uiEvent.shareId
                                    ).also(onNavigateEvent)
                                }

                                is HomeDrawerUiEvent.OnManageVaultClick -> {
                                    HomeNavigation.ManageVault(
                                        shareId = uiEvent.shareId
                                    ).also(onNavigateEvent)
                                }

                                is HomeDrawerUiEvent.OnVaultOptionsClick -> {
                                    HomeNavigation.VaultOptions(
                                        shareId = uiEvent.shareId
                                    ).also(onNavigateEvent)
                                }

                                HomeDrawerUiEvent.OnSharedWithMeClick -> {
                                    scope.launch { drawerState.close() }

                                    VaultSelectionOption.SharedWithMe
                                        .also(homeDrawerViewModel::setVaultSelection)
                                        .also(homeViewModel::setVaultSelection)
                                }

                                HomeDrawerUiEvent.OnSharedByMeClick -> {
                                    scope.launch { drawerState.close() }

                                    VaultSelectionOption.SharedByMe
                                        .also(homeDrawerViewModel::setVaultSelection)
                                        .also(homeViewModel::setVaultSelection)
                                }

                                HomeDrawerUiEvent.OnTrashClick -> {
                                    scope.launch { drawerState.close() }

                                    VaultSelectionOption.Trash
                                        .also(homeDrawerViewModel::setVaultSelection)
                                        .also(homeViewModel::setVaultSelection)
                                }

                                HomeDrawerUiEvent.OnCreateVaultClick -> {
                                    onNavigateEvent(HomeNavigation.CreateVault)
                                }
                            }
                        }
                    )
                } else {
                    val drawerUiState by vaultDrawerViewModel.drawerUiState.collectAsStateWithLifecycle()

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
                                    homeUiEvent.item.id,
                                    homeUiEvent.item.category
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
                                    is ItemContents.Identity -> IdentityOptions
                                    is ItemContents.Custom -> CustomOptions
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
                            if (homeUiState.homeListUiState.isItemSelectable(homeUiEvent.item)) {
                                homeViewModel.onItemSelected(homeUiEvent.item)
                                return@HomeContent
                            }

                            if (homeUiEvent.item.isSharedWithMe) {
                                homeViewModel.onSharedWithMeItemSelected()
                                return@HomeContent
                            }

                            homeViewModel.onReadOnlyItemSelected()
                        }

                        HomeUiEvent.MoveToTrashItemsActionClick -> {
                            shouldShowMoveToTrashItemsDialog = true
                        }

                        is HomeUiEvent.StopBulk -> {
                            homeViewModel.clearSelection()
                        }

                        HomeUiEvent.PermanentlyDeleteItemsActionClick -> {
                            val containsAlias =
                                homeUiState.homeListUiState.selectionState.selectedItems
                                    .any { it.contents is ItemContents.Alias }
                            if (containsAlias) {
                                shouldShowBulkDeleteAliasDialog = true
                            } else {
                                shouldShowDeleteItemsDialog = true
                            }
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

                        HomeUiEvent.DisableAliasItemsActionClick -> {
                            homeViewModel.disableSelectedAliasItems(
                                items = homeUiState.homeListUiState.selectionState.selectedItems
                            )
                        }

                        HomeUiEvent.EnableAliasItemsActionClick -> {
                            homeViewModel.enableSelectedAliasItems(
                                items = homeUiState.homeListUiState.selectionState.selectedItems
                            )
                        }
                    }
                }
            )

            ConfirmRestoreAllDialog(
                show = shouldShowRestoreAllDialog,
                isLoading = actionState is ActionState.Loading,
                onDismiss = {
                    shouldShowRestoreAllDialog = false
                },
                onConfirm = {
                    homeViewModel.restoreAllItems()
                }
            )

            ConfirmClearTrashDialog(
                show = shouldShowClearTrashDialog,
                hasSharedTrashedItems = homeUiState.hasSharedTrashedItems,
                sharedTrashedItemsCount = homeUiState.sharedTrashedItemsCount,
                isLoading = actionState is ActionState.Loading,
                onDismiss = {
                    shouldShowClearTrashDialog = false
                },
                onConfirm = {
                    homeViewModel.clearTrash()
                }
            )

            ConfirmDeleteItemDialog(
                isLoading = actionState is ActionState.Loading,
                show = shouldShowDeleteItemDialog,
                isSharedItem = selectedItem?.isShared == true,
                onConfirm = {
                    selectedItem?.let {
                        homeViewModel.deleteItems(listOf(it))
                    }
                },
                onDismiss = { shouldShowDeleteItemDialog = false }
            )

            ConfirmTrashAliasDialog(
                show = shouldShowMoveToTrashAliasDialog,
                onConfirm = {
                    val item = selectedItem ?: return@ConfirmTrashAliasDialog
                    homeViewModel.sendItemsToTrash(listOf(item))
                    selectedItem = null
                },
                onDismiss = {
                    shouldShowMoveToTrashAliasDialog = false
                    selectedItem = null
                }
            )

            ConfirmRestoreItemsDialog(
                show = shouldShowRestoreItemsDialog,
                isLoading = actionState is ActionState.Loading,
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
                isLoading = actionState is ActionState.Loading,
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
                isLoading = actionState is ActionState.Loading,
                amount = homeUiState.homeListUiState.selectionState.selectedItems.size,
                hasSelectedSharedItems = homeUiState.homeListUiState.selectionState.hasSelectedSharedItems,
                sharedSelectedItemsCount = homeUiState.homeListUiState.selectionState.selectedSharedItemsCount,
                onConfirm = {
                    homeViewModel.deleteItems(
                        homeUiState.homeListUiState.selectionState.selectedItems
                    )
                },
                onDismiss = { shouldShowDeleteItemsDialog = false }
            )

            ConfirmDeleteEnabledAliasDialog(
                show = shouldShowDeleteEnabledAliasDialog,
                isDeleteLoading = actionState is ActionState.Loading &&
                    actionState.loadingDialog == LoadingDialog.Other,
                isDisableLoading = actionState is ActionState.Loading &&
                    actionState.loadingDialog == LoadingDialog.DisableAlias,
                alias = (selectedItem?.contents as? ItemContents.Alias)?.aliasEmail.orEmpty(),
                onDelete = {
                    val item = selectedItem ?: return@ConfirmDeleteEnabledAliasDialog
                    homeViewModel.deleteItems(listOf(item))
                    selectedItem = null
                },
                onDisable = {
                    val item = selectedItem ?: return@ConfirmDeleteEnabledAliasDialog
                    homeViewModel.disableAlias(item)
                    selectedItem = null
                },
                onDismiss = {
                    shouldShowDeleteEnabledAliasDialog = false
                    selectedItem = null
                }
            )

            ConfirmDeleteDisabledAliasDialog(
                show = shouldShowDeleteDisabledAliasDialog,
                isLoading = actionState is ActionState.Loading,
                alias = (selectedItem?.contents as? ItemContents.Alias)?.aliasEmail.orEmpty(),
                onConfirm = {
                    val item = selectedItem ?: return@ConfirmDeleteDisabledAliasDialog
                    homeViewModel.deleteItems(listOf(item))
                    selectedItem = null
                },
                onDismiss = {
                    shouldShowDeleteDisabledAliasDialog = false
                    selectedItem = null
                }
            )

            ConfirmBulkDeleteAliasDialog(
                show = shouldShowBulkDeleteAliasDialog,
                isLoading = actionState is ActionState.Loading,
                aliasCount = homeUiState.homeListUiState.selectionState.selectedItems
                    .filter { it.contents is ItemContents.Alias }
                    .size,
                onConfirm = {
                    homeViewModel.deleteItems(
                        homeUiState.homeListUiState.selectionState.selectedItems
                    )
                },
                onDismiss = {
                    shouldShowBulkDeleteAliasDialog = false
                }
            )
        }
    }
}

object HomeScreenTestTag {
    const val SCREEN = "homeScreen"
}
