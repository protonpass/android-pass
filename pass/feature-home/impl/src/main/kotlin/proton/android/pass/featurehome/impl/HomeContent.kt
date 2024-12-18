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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
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
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.TestTags.HOME_EMPTY_TAG
import proton.android.pass.commonui.api.TestTags.HOME_ITEM_LIST_TAG
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.AllVaultsIcon
import proton.android.pass.composecomponents.impl.icon.TrashVaultIcon
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.item.ItemsList
import proton.android.pass.composecomponents.impl.item.header.ItemCount
import proton.android.pass.composecomponents.impl.item.header.ItemListHeader
import proton.android.pass.composecomponents.impl.item.header.SortingButton
import proton.android.pass.composecomponents.impl.item.icon.ThreeDotsMenuButton
import proton.android.pass.composecomponents.impl.pinning.PinCarousel
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.ArrowBackIconButton
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.featurehome.impl.HomeContentTestTag.DRAWER_ICON_TEST_TAG
import proton.android.pass.searchoptions.api.SearchFilterType
import proton.android.pass.searchoptions.api.VaultSelectionOption

@Suppress("ComplexMethod")
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    shouldScrollToTop: Boolean,
    scrollableState: LazyListState,
    header: LazyListScope.() -> Unit = {},
    onEvent: (HomeUiEvent) -> Unit
) {
    val isTrashMode = uiState.homeListUiState.homeVaultSelection == VaultSelectionOption.Trash
    val isPinningOrSearch =
        remember(uiState.pinningUiState.inPinningMode, uiState.searchUiState.inSearchMode) {
            uiState.pinningUiState.inPinningMode || uiState.searchUiState.inSearchMode
        }
    Scaffold(
        modifier = modifier,
        topBar = {
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
                SearchTopBar(
                    searchQuery = uiState.searchUiState.searchQuery,
                    inSearchMode = uiState.searchUiState.inSearchMode,
                    placeholderText = if (uiState.pinningUiState.inPinningMode) {
                        stringResource(R.string.search_topbar_placeholder_pinning)
                    } else {
                        when (uiState.homeListUiState.homeVaultSelection) {
                            VaultSelectionOption.AllVaults ->
                                stringResource(R.string.search_topbar_placeholder_all_vaults)

                            VaultSelectionOption.Trash ->
                                stringResource(R.string.search_topbar_placeholder_trash)

                            is VaultSelectionOption.Vault -> stringResource(
                                R.string.search_topbar_placeholder_vault,
                                uiState.homeListUiState.selectedShare.value()?.name ?: ""
                            )
                        }
                    },
                    onEnterSearch = { onEvent(HomeUiEvent.EnterSearch) },
                    onStopSearch = { onEvent(HomeUiEvent.StopSearch) },
                    onSearchQueryChange = { onEvent(HomeUiEvent.SearchQueryChange(it)) },
                    drawerIcon = {
                        HomeDrawerIcon(
                            modifier = Modifier.testTag(DRAWER_ICON_TEST_TAG),
                            selectedShare = uiState.homeListUiState.selectedShare,
                            homeVaultSelection = uiState.homeListUiState.homeVaultSelection,
                            isSeeAllPinsMode = uiState.pinningUiState.inPinningMode,
                            isSearchMode = uiState.searchUiState.inSearchMode,
                            isItemSharingEnabled = uiState.isItemSharingEnabled,
                            onEvent = onEvent
                        )
                    },
                    actions = {
                        val (backgroundColor, iconColor) =
                            if (uiState.homeListUiState.searchFilterType != SearchFilterType.All) {
                                PassTheme.colors.interactionNormMajor2 to PassTheme.colors.textInvert
                            } else {
                                Color.Transparent to PassTheme.colors.textWeak
                            }
                        ThreeDotsMenuButton(
                            modifier = Modifier.clip(CircleShape).background(backgroundColor),
                            dotsColor = iconColor
                        ) { onEvent(HomeUiEvent.ActionsClick) }
                    }
                )
            }
        }
    ) { contentPadding ->
        val keyboardController = LocalSoftwareKeyboardController.current
        val firstItemVisible by remember {
            derivedStateOf {
                scrollableState.firstVisibleItemIndex <= 1
            }
        }
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
                    if (!readOnly && !isPinningOrSearch) {
                        onEvent(HomeUiEvent.SelectItem(it))
                    }
                },
                onScrollToTop = { onEvent(HomeUiEvent.ScrollToTop) },
                canLoadExternalImages = uiState.homeListUiState.canLoadExternalImages,
                isInSelectionMode = uiState.homeListUiState.selectionState.isInSelectMode,
                selectedItemIds = selectedItemIds,
                emptyContent = {
                    HomeEmptyContent(
                        modifier = Modifier.testTag(HOME_EMPTY_TAG),
                        isTrashMode = isTrashMode,
                        inSearchMode = isPinningOrSearch,
                        filterType = uiState.homeListUiState.searchFilterType,
                        readOnly = uiState.isSelectedVaultReadOnly(),
                        shareId = uiState.homeListUiState.selectedShare.map { it.id },
                        onEvent = onEvent
                    )
                },
                accounts = persistentMapOf()
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HomeDrawerIcon(
    modifier: Modifier = Modifier,
    selectedShare: Option<ShareUiModel>,
    homeVaultSelection: VaultSelectionOption,
    isSeeAllPinsMode: Boolean,
    isSearchMode: Boolean,
    isItemSharingEnabled: Boolean,
    onEvent: (HomeUiEvent) -> Unit
) {
    if (!isSeeAllPinsMode && !isSearchMode) {
        when (selectedShare) {
            None -> {
                when (homeVaultSelection) {
                    VaultSelectionOption.AllVaults -> {
                        AllVaultsIcon(
                            modifier = modifier,
                            isItemSharingEnabled = isItemSharingEnabled,
                            size = 48,
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

                    else -> {} // This combination is not possible
                }
            }

            is Some -> {
                VaultIcon(
                    modifier = modifier.size(48.dp),
                    backgroundColor = selectedShare.value.color.toColor(true),
                    iconColor = selectedShare.value.color.toColor(),
                    icon = selectedShare.value.icon.toResource(),
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
