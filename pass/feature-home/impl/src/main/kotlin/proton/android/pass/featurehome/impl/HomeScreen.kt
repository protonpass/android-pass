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
import androidx.compose.runtime.DisposableEffect
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
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.toClassHolder
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.composecomponents.impl.bottomsheet.PassModalBottomSheetLayout
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import proton.android.pass.featurehome.impl.HomeBottomSheetType.AliasOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.CreditCardOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.IdentityOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.LoginOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.NoteOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.TrashItemOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.TrashOptions
import proton.android.pass.featurehome.impl.HomeBottomSheetType.Unknown
import proton.android.pass.featurehome.impl.HomeNavigation.SortingBottomsheet
import proton.android.pass.featurehome.impl.bottomsheet.AliasOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.CreditCardOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.IdentityOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.LoginOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.NoteOptionsBottomSheetContents
import proton.android.pass.featurehome.impl.bottomsheet.TrashAllBottomSheetContents
import proton.android.pass.featurehome.impl.needsupdate.AppNeedsUpdateBanner
import proton.android.pass.featurehome.impl.onboardingtips.NotificationPermissionLaunchedEffect
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTips
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipsEvent
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipsViewModel
import proton.android.pass.featurehome.impl.saver.HomeBottomSheetTypeSaver
import proton.android.pass.featurehome.impl.saver.ItemUiModelSaver
import proton.android.pass.featurehome.impl.trash.ConfirmClearTrashDialog
import proton.android.pass.featurehome.impl.trash.ConfirmDeleteItemsDialog
import proton.android.pass.featurehome.impl.trash.ConfirmRestoreAllDialog
import proton.android.pass.featurehome.impl.trash.ConfirmRestoreItemsDialog
import proton.android.pass.featurehome.impl.trash.ConfirmTrashItemsDialog
import proton.android.pass.featurehome.impl.vault.VaultDrawerContent
import proton.android.pass.featurehome.impl.vault.VaultDrawerViewModel
import proton.android.pass.features.trash.ConfirmDeleteDisabledAliasDialog
import proton.android.pass.features.trash.ConfirmDeleteEnabledAliasDialog
import proton.android.pass.features.trash.ConfirmDeleteItemDialog
import proton.android.pass.features.trash.ConfirmTrashAliasDialog
import proton.android.pass.features.trash.TrashItemBottomSheetContents
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption

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
    val routerEvent by routerViewModel.routerEventState.collectAsStateWithLifecycle(RouterEvent.None)
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
    var shouldShowMoveToTrashAliasDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowDeleteEnabledAliasDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowDeleteDisabledAliasDialog by rememberSaveable { mutableStateOf(false) }
    var shouldShowBulkDeleteAliasDialog by rememberSaveable { mutableStateOf(false) }
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
            shouldShowMoveToTrashAliasDialog = false
            shouldShowDeleteEnabledAliasDialog = false
            shouldShowDeleteDisabledAliasDialog = false
            shouldShowBulkDeleteAliasDialog = false
            homeViewModel.restoreActionState()
        }
    }

    DisposableEffect(routerEvent) {
        when (routerEvent) {
            RouterEvent.OnBoarding -> onNavigateEvent(HomeNavigation.OnBoarding)
            RouterEvent.ConfirmedInvite -> onNavigateEvent(HomeNavigation.ConfirmedInvite)
            RouterEvent.SyncDialog -> onNavigateEvent(HomeNavigation.SyncDialog)
            RouterEvent.None -> {}
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
            OnBoardingTipsEvent.OpenInviteScreen -> HomeNavigation.OpenInvite
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
                        isFreePlan = homeUiState.isFreePlan
                    )
                }

                AliasOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    AliasOptionsBottomSheetContents(
                        itemUiModel = item,
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
                        isFreePlan = homeUiState.isFreePlan
                    )
                }

                CreditCardOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    CreditCardOptionsBottomSheetContents(
                        itemUiModel = item,
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
                        isFreePlan = homeUiState.isFreePlan
                    )
                }

                IdentityOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    IdentityOptionsBottomSheetContents(
                        itemUiModel = item,
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

                TrashItemOptions -> {
                    val item = selectedItem ?: return@PassModalBottomSheetLayout
                    TrashItemBottomSheetContents(
                        itemUiModel = item,
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

            ConfirmDeleteEnabledAliasDialog(
                show = shouldShowDeleteEnabledAliasDialog,
                isDeleteLoading = false,
                isDisableLoading = false,
                alias = (selectedItem?.contents as? ItemContents.Alias)?.aliasEmail.orEmpty(),
                onDelete = {
                    val item = selectedItem ?: return@ConfirmDeleteEnabledAliasDialog
                    homeViewModel.deleteItems(listOf(item))
                    selectedItem = null
                },
                onDisable = {
                    val item = selectedItem ?: return@ConfirmDeleteEnabledAliasDialog
                    homeViewModel.disableSelectedAliasItems(listOf(item).toPersistentList())
                    selectedItem = null
                },
                onDismiss = {
                    shouldShowDeleteEnabledAliasDialog = false
                    selectedItem = null
                }
            )

            ConfirmDeleteDisabledAliasDialog(
                show = shouldShowDeleteDisabledAliasDialog,
                isLoading = false,
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
        }
    }
}

object HomeScreenTestTag {
    const val SCREEN = "homeScreen"
}
