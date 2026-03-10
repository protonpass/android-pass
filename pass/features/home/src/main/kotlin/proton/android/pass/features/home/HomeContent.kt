/*
 * Copyright (c) 2023-2026 Proton AG
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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentSet
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.commonui.api.TestTags.HOME_EMPTY_TAG
import proton.android.pass.commonui.api.TestTags.HOME_ITEM_LIST_TAG
import proton.android.pass.composecomponents.impl.buttons.UpgradeIcon
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.AllVaultsIcon
import proton.android.pass.composecomponents.impl.icon.FolderIcon
import proton.android.pass.composecomponents.impl.icon.PromoIcon
import proton.android.pass.composecomponents.impl.icon.TrashVaultIcon
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.item.ItemsList
import proton.android.pass.composecomponents.impl.item.header.ItemCount
import proton.android.pass.composecomponents.impl.item.header.ItemListHeader
import proton.android.pass.composecomponents.impl.item.header.SortingButton
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.pinning.PinCarousel
import proton.android.pass.composecomponents.impl.topbar.CollapsibleSearchTopBar
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.ArrowBackIconButton
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.features.home.HomeContentTestTag.DRAWER_ICON_TEST_TAG
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.searchoptions.api.VaultSelectionOption
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as ComponentsR

@Suppress("ComplexMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    shouldScrollToTop: Boolean,
    canCreateVault: Boolean,
    scrollableState: LazyListState,
    header: LazyListScope.() -> Unit = {},
    onEvent: (HomeUiEvent) -> Unit
) {
    val isTrashMode = remember(uiState.homeListUiState.homeVaultSelection) {
        uiState.homeListUiState.homeVaultSelection is VaultSelectionOption.Trash
    }
    val isPinningOrSearch =
        remember(uiState.pinningUiState.inPinningMode, uiState.searchUiState.inSearchMode) {
            uiState.pinningUiState.inPinningMode || uiState.searchUiState.inSearchMode
        }
    val firstItemVisible by remember {
        derivedStateOf {
            scrollableState.firstVisibleItemIndex <= 1
        }
    }
    val flingSpec = rememberSplineBasedDecay<Float>()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        snapAnimationSpec = spring(),
        flingAnimationSpec = flingSpec
    )
    val barScrollConnection = rememberHomeTopBarScrollConnection(scrollBehavior)
    Scaffold(
        modifier = modifier
            .statusBarsPadding()
            .applyIf(uiState.foldersEnabled, ifTrue = { nestedScroll(barScrollConnection) }),
        topBar = {
            val placeholderText = if (uiState.pinningUiState.inPinningMode) {
                stringResource(R.string.search_topbar_placeholder_pinning)
            } else {
                when (uiState.homeListUiState.homeVaultSelection) {
                    VaultSelectionOption.AllVaults ->
                        stringResource(R.string.search_topbar_placeholder_all_items)
                    VaultSelectionOption.Trash ->
                        stringResource(R.string.search_topbar_placeholder_trash)
                    is VaultSelectionOption.Vault -> stringResource(
                        R.string.search_topbar_placeholder_vault,
                        uiState.homeListUiState.selectedVaultName
                    )
                    VaultSelectionOption.SharedByMe ->
                        stringResource(id = R.string.search_topbar_placeholder_shared_by_me)
                    VaultSelectionOption.SharedWithMe ->
                        stringResource(id = R.string.search_topbar_placeholder_shared_with_me)
                    is VaultSelectionOption.Folder ->
                        uiState.homeListUiState.selectedFolderName
                            .takeIf { it.isNotBlank() }
                            ?.let { stringResource(R.string.search_topbar_placeholder_folder, it) }
                            ?: stringResource(R.string.search_topbar_placeholder_folder_fallback)
                }
            }
            if (uiState.isTopBarAvailable) {
                AnimatedVisibility(
                    visible = uiState.homeListUiState.selectionState.isInSelectMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut(),
                    label = "HomeContent-SelectionModeTopBar"
                ) {
                    SelectionModeTopBar(
                        selectionState = uiState.homeListUiState.selectionState.topBarState,
                        onEvent = onEvent
                    )
                }
                AnimatedVisibility(
                    visible = !uiState.homeListUiState.selectionState.isInSelectMode,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut(),
                    label = "HomeContent-SearchTopBar"
                ) {
                    if (uiState.foldersEnabled) {
                        val topBarTitle = if (uiState.pinningUiState.inPinningMode) {
                            stringResource(ComponentsR.string.item_list_header_pinned_search_results)
                        } else {
                            when (uiState.homeListUiState.homeVaultSelection) {
                                VaultSelectionOption.AllVaults ->
                                    stringResource(R.string.home_drawer_all_items)
                                VaultSelectionOption.Trash ->
                                    stringResource(R.string.vault_drawer_item_trash)
                                is VaultSelectionOption.Vault ->
                                    uiState.homeListUiState.selectedVaultName
                                VaultSelectionOption.SharedByMe ->
                                    stringResource(R.string.item_type_filter_items_shared_by_me)
                                VaultSelectionOption.SharedWithMe ->
                                    stringResource(R.string.item_type_filter_items_shared_with_me)
                                is VaultSelectionOption.Folder ->
                                    uiState.homeListUiState.selectedFolderName
                                        .takeIf { it.isNotBlank() }
                                        ?: stringResource(R.string.home_topbar_title_folder_fallback)
                            }
                        }
                        CollapsibleSearchTopBar(
                            scrollBehavior = scrollBehavior,
                            title = topBarTitle,
                            searchQuery = uiState.searchUiState.searchQuery,
                            inSearchMode = uiState.searchUiState.inSearchMode,
                            placeholderText = placeholderText,
                            onEnterSearch = { onEvent(HomeUiEvent.EnterSearch) },
                            onStopSearch = { onEvent(HomeUiEvent.StopSearch) },
                            onSearchQueryChange = { onEvent(HomeUiEvent.SearchQueryChange(it)) },
                            drawerIcon = {
                                HomeDrawerIcon(
                                    modifier = Modifier.testTag(DRAWER_ICON_TEST_TAG),
                                    selectedVaultOption = uiState.homeListUiState.selectedVaultOption,
                                    homeVaultSelection = uiState.homeListUiState.homeVaultSelection,
                                    isSeeAllPinsMode = uiState.pinningUiState.inPinningMode,
                                    isSearchMode = uiState.searchUiState.inSearchMode,
                                    onEvent = onEvent
                                )
                            },
                            actions = {
                                HomeTopBarActions(uiState = uiState, onEvent = onEvent)
                            }
                        )
                    } else {
                        SearchTopBar(
                            searchQuery = uiState.searchUiState.searchQuery,
                            inSearchMode = uiState.searchUiState.inSearchMode,
                            placeholderText = placeholderText,
                            onEnterSearch = { onEvent(HomeUiEvent.EnterSearch) },
                            onStopSearch = { onEvent(HomeUiEvent.StopSearch) },
                            onSearchQueryChange = { onEvent(HomeUiEvent.SearchQueryChange(it)) },
                            drawerIcon = {
                                HomeDrawerIcon(
                                    modifier = Modifier.testTag(DRAWER_ICON_TEST_TAG),
                                    selectedVaultOption = uiState.homeListUiState.selectedVaultOption,
                                    homeVaultSelection = uiState.homeListUiState.homeVaultSelection,
                                    isSeeAllPinsMode = uiState.pinningUiState.inPinningMode,
                                    isSearchMode = uiState.searchUiState.inSearchMode,
                                    onEvent = onEvent
                                )
                            },
                            actions = {
                                HomeTopBarActions(uiState = uiState, onEvent = onEvent)
                            }
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        val keyboardController = LocalSoftwareKeyboardController.current
        val listItemCount = remember(uiState.homeListUiState.items) {
            uiState.homeListUiState.items.map { it.items }.flatten().count()
        }
        val pinningItemsCount = remember(uiState.pinningUiState.filteredItems) {
            uiState.pinningUiState.filteredItems.map { it.items }.flatten().count()
        }
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            if (!isPinningOrSearch && !isTrashMode) {
                PinCarousel(
                    modifier = Modifier.height(48.dp),
                    list = uiState.pinningUiState.unFilteredItems,
                    canLoadExternalImages = uiState.homeListUiState.canLoadExternalImages,
                    onItemClick = { item -> onEvent(HomeUiEvent.ItemClick(item)) },
                    onSeeAllClick = { onEvent(HomeUiEvent.SeeAllPinned) }
                )

                if (uiState.pinningUiState.unFilteredItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(Spacing.medium))
                }
            }
            AnimatedVisibility(
                visible = isPinningOrSearch && firstItemVisible,
                label = "HomeContent-ItemTypeFilterList"
            ) {
                val itemTypeCount = if (uiState.pinningUiState.inPinningMode) {
                    uiState.pinningUiState.itemTypeCount
                } else {
                    uiState.searchUiState.itemTypeCount
                }
                ItemTypeFilterList(
                    selected = uiState.homeListUiState.searchFilterType,
                    itemTypeCount = itemTypeCount,
                    onItemTypeClick = { onEvent(HomeUiEvent.ItemTypeSelected(it)) }
                )
            }

            val showItemListHeader = remember(uiState) { uiState.shouldShowItemListHeader() }
            if (showItemListHeader) {
                ItemListHeader(
                    countContent = {
                        val itemCount = if (uiState.pinningUiState.inPinningMode) {
                            pinningItemsCount
                        } else {
                            listItemCount
                        }
                        ItemCount(
                            modifier = Modifier.padding(
                                Spacing.medium,
                                Spacing.none,
                                Spacing.none,
                                Spacing.none
                            ),
                            showSearchResults = isPinningOrSearch && uiState.searchUiState.searchQuery.isNotEmpty(),
                            itemType = uiState.homeListUiState.searchFilterType,
                            itemCount = itemCount.takeIf { !uiState.searchUiState.isProcessingSearch.value() },
                            isPinnedMode = uiState.pinningUiState.inPinningMode
                        )
                    },
                    sortingContent = {
                        SortingButton(
                            sortingType = uiState.homeListUiState.sortingType,
                            onSortingOptionsClick = { onEvent(HomeUiEvent.SortingOptionsClick) }
                        )
                    }
                )
            }

            val showRecentSearchHeader =
                remember(uiState) { uiState.shouldShowRecentSearchHeader() }
            if (showRecentSearchHeader) {
                val itemCount = remember(uiState.homeListUiState.items) {
                    uiState.homeListUiState.items.map { it.items }.flatten().count()
                }
                RecentSearchListHeader(
                    itemCount = itemCount,
                    onClearRecentSearchClick = { onEvent(HomeUiEvent.ClearRecentSearchClick) }
                )
            }

            val forceShowHeader = remember(uiState.searchUiState) {
                !uiState.searchUiState.inSearchMode
            }

            val items = if (!uiState.pinningUiState.inPinningMode) {
                uiState.homeListUiState.items
            } else {
                uiState.pinningUiState.filteredItems
            }

            val selectedItemIds: PersistentSet<Pair<ShareId, ItemId>> = remember(
                key1 = uiState.homeListUiState.selectionState.selectedItems
            ) {
                uiState.homeListUiState.selectionState.selectedItems
                    .map { it.shareId to it.id }
                    .toPersistentSet()
            }

            ItemsList(
                modifier = Modifier.testTag(HOME_ITEM_LIST_TAG),
                items = items,
                shares = uiState.homeListUiState.shares,
                isShareSelected = uiState.homeListUiState.selectedShare.isNotEmpty(),
                scrollableState = scrollableState,
                shouldScrollToTop = shouldScrollToTop,
                highlight = uiState.searchUiState.searchQuery,
                isRefreshing = uiState.homeListUiState.isRefreshing,
                isLoading = uiState.homeListUiState.isLoading,
                isProcessingSearch = uiState.searchUiState.isProcessingSearch,
                forceShowHeader = forceShowHeader,
                header = header,
                onRefresh = { onEvent(HomeUiEvent.Refresh) },
                onItemClick = { item ->
                    keyboardController?.hide()
                    if (uiState.homeListUiState.selectionState.isInSelectMode) {
                        onEvent(HomeUiEvent.SelectItem(item))
                    } else {
                        onEvent(HomeUiEvent.ItemClick(item))
                    }
                },
                onItemMenuClick = { onEvent(HomeUiEvent.ItemMenuClick(it)) },
                onItemLongClick = {
                    val readOnly = uiState.isSelectedVaultReadOnly()
                    if (!readOnly) {
                        onEvent(HomeUiEvent.SelectItem(it))
                    }
                },
                onScrollToTop = { onEvent(HomeUiEvent.ScrollToTop) },
                canLoadExternalImages = uiState.homeListUiState.canLoadExternalImages,
                isInSelectionMode = uiState.homeListUiState.selectionState.isInSelectMode,
                selectedItemIds = selectedItemIds,
                emptyContent = {
                    val selectedFolder = uiState.homeListUiState.selectedFolder.value()
                    val shareId = selectedFolder?.shareId ?: uiState.homeListUiState.selectedShare.map { it.id }.value()
                    val folderId = selectedFolder?.folderId
                    HomeEmptyContent(
                        modifier = Modifier.testTag(HOME_EMPTY_TAG),
                        hasShares = uiState.hasShares,
                        canCreateItems = uiState.canCreateItems,
                        canCreateVault = canCreateVault,
                        vaultSelectionOption = uiState.homeListUiState.homeVaultSelection,
                        inSearchMode = isPinningOrSearch,
                        filterType = uiState.homeListUiState.searchFilterType,
                        readOnly = uiState.isSelectedVaultReadOnly(),
                        shareId = shareId.toOption(),
                        folderId = folderId.toOption(),
                        onEvent = onEvent
                    )
                },
                accounts = persistentMapOf()
            )
        }
    }
}

@Composable
private fun HomeTopBarActions(uiState: HomeUiState, onEvent: (HomeUiEvent) -> Unit) {
    uiState.homeListUiState.promoInAppMessage?.let { promo ->
        PromoIcon(
            onClick = {
                onEvent(
                    HomeUiEvent.PromoInAppMessageClick(
                        userId = promo.userId,
                        inAppMessageId = promo.id
                    )
                )
            }
        )
    } ?: run {
        if (uiState.isUpgradeAvailable) {
            UpgradeIcon(onUpgradeClick = { onEvent(HomeUiEvent.OnUpgradeClick) })
        }
    }

    val (backgroundColor, iconColor) =
        if (uiState.homeListUiState.searchFilterType != SearchFilterType.All) {
            PassTheme.colors.interactionNormMajor2 to PassTheme.colors.textInvert
        } else {
            Color.Transparent to PassTheme.colors.textWeak
        }
    ThreeDotsMenuButton(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor),
        dotsColor = iconColor
    ) { onEvent(HomeUiEvent.ActionsClick) }
}

@Composable
private fun HomeDrawerIcon(
    modifier: Modifier = Modifier,
    selectedVaultOption: Option<Vault>,
    homeVaultSelection: VaultSelectionOption,
    isSeeAllPinsMode: Boolean,
    isSearchMode: Boolean,
    onEvent: (HomeUiEvent) -> Unit
) {
    if (!isSeeAllPinsMode && !isSearchMode) {
        when (selectedVaultOption) {
            None -> {
                when (homeVaultSelection) {
                    is VaultSelectionOption.Vault,
                    VaultSelectionOption.AllVaults -> {
                        AllVaultsIcon(
                            modifier = modifier,
                            size = 48,
                            onClick = { onEvent(HomeUiEvent.DrawerIconClick) }
                        )
                    }

                    VaultSelectionOption.SharedWithMe -> {
                        VaultIcon(
                            modifier = modifier.size(48.dp),
                            icon = CoreR.drawable.ic_proton_user_arrow_left,
                            iconColor = PassTheme.colors.interactionNormMajor2,
                            backgroundColor = PassTheme.colors.interactionNormMinor1,
                            onClick = { onEvent(HomeUiEvent.DrawerIconClick) }
                        )
                    }
                    VaultSelectionOption.SharedByMe -> {
                        VaultIcon(
                            modifier = modifier.size(48.dp),
                            icon = CoreR.drawable.ic_proton_user_arrow_right,
                            iconColor = PassTheme.colors.interactionNormMajor2,
                            backgroundColor = PassTheme.colors.interactionNormMinor1,
                            onClick = { onEvent(HomeUiEvent.DrawerIconClick) }
                        )
                    }

                    VaultSelectionOption.Trash -> {
                        TrashVaultIcon(
                            modifier = modifier,
                            size = 48,
                            iconSize = 20,
                            onClick = { onEvent(HomeUiEvent.DrawerIconClick) }
                        )
                    }

                    is VaultSelectionOption.Folder -> {
                        FolderIcon(
                            modifier = modifier.size(48.dp),
                            onClick = { onEvent(HomeUiEvent.DrawerIconClick) }
                        )
                    }
                }
            }

            is Some -> {
                VaultIcon(
                    modifier = modifier.size(48.dp),
                    backgroundColor = selectedVaultOption.value.color.toColor(true),
                    iconColor = selectedVaultOption.value.color.toColor(),
                    icon = selectedVaultOption.value.icon.toResource(),
                    onClick = { onEvent(HomeUiEvent.DrawerIconClick) }
                )
            }
        }
    } else {
        ArrowBackIconButton(
            modifier = modifier,
            onUpClick = {
                if (isSearchMode) {
                    HomeUiEvent.StopSearch
                } else {
                    HomeUiEvent.StopSeeAllPinned
                }.also(onEvent)
            }
        )
    }
}

object HomeContentTestTag {
    const val DRAWER_ICON_TEST_TAG = "drawerIcon"
}
