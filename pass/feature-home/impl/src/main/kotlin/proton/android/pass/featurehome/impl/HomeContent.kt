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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemTypeUiState
import proton.android.pass.composecomponents.impl.bottombar.BottomBar
import proton.android.pass.composecomponents.impl.bottombar.BottomBarSelected
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.AllVaultsIcon
import proton.android.pass.composecomponents.impl.icon.TrashVaultIcon
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.item.EmptySearchResults
import proton.android.pass.composecomponents.impl.item.ItemsList
import proton.android.pass.composecomponents.impl.item.header.ItemListHeader
import proton.android.pass.composecomponents.impl.item.header.SortingButton
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.ArrowBackIconButton
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featurehome.impl.HomeContentTestTag.DrawerIconTestTag
import proton.android.pass.featurehome.impl.empty.HomeEmptyList
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTips
import proton.android.pass.featurehome.impl.trash.EmptyTrashContent
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import me.proton.core.presentation.R as CoreR

@Suppress("LongParameterList", "ComplexMethod")
@ExperimentalComposeUiApi
@ExperimentalMaterialApi
@Composable
internal fun HomeContent(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    shouldScrollToTop: Boolean,
    onEvent: (HomeUiEvent) -> Unit
) {
    val isTrashMode = uiState.homeListUiState.homeVaultSelection == VaultSelectionOption.Trash
    Scaffold(
        modifier = modifier,
        topBar = {
            SearchTopBar(
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                placeholderText = when (uiState.homeListUiState.homeVaultSelection) {
                    VaultSelectionOption.AllVaults -> stringResource(R.string.search_topbar_placeholder_all_vaults)
                    VaultSelectionOption.Trash -> stringResource(R.string.search_topbar_placeholder_trash)
                    is VaultSelectionOption.Vault -> stringResource(
                        R.string.search_topbar_placeholder_vault,
                        uiState.homeListUiState.selectedShare.value()?.name ?: ""
                    )
                },
                onEnterSearch = { onEvent(HomeUiEvent.EnterSearch) },
                onStopSearch = { onEvent(HomeUiEvent.StopSearch) },
                onSearchQueryChange = { onEvent(HomeUiEvent.SearchQueryChange(it)) },
                drawerIcon = {
                    HomeDrawerIcon(
                        modifier = Modifier.testTag(DrawerIconTestTag),
                        uiState = uiState,
                        onDrawerIconClick = { onEvent(HomeUiEvent.DrawerIconClick) },
                        onStopSearch = { onEvent(HomeUiEvent.StopSearch) }
                    )
                },
                actions = {
                    IconButton(onClick = { onEvent(HomeUiEvent.ActionsClick) }) {
                        Icon(
                            painter = painterResource(CoreR.drawable.ic_proton_three_dots_vertical),
                            contentDescription = null,
                            tint = PassTheme.colors.textWeak
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomBar(
                bottomBarSelected = BottomBarSelected.Home,
                accountType = uiState.accountType,
                onListClick = {},
                onCreateClick = {
                    val shareId = uiState.homeListUiState.selectedShare.map { it.id }
                    onEvent(HomeUiEvent.AddItemClick(shareId, ItemTypeUiState.Unknown))
                },
                onProfileClick = { onEvent(HomeUiEvent.ProfileClick) }
            )
        }
    ) { contentPadding ->
        val keyboardController = LocalSoftwareKeyboardController.current
        val scrollableState = rememberLazyListState()
        val firstItemVisible by remember {
            derivedStateOf {
                scrollableState.firstVisibleItemIndex <= 1
            }
        }
        Column(
            modifier = Modifier.padding(contentPadding)
        ) {
            AnimatedVisibility(
                visible = uiState.searchUiState.inSearchMode && firstItemVisible,
                label = "HomeContent-ItemTypeFilterList"
            ) {
                ItemTypeFilterList(
                    selected = uiState.homeListUiState.searchFilterType,
                    loginCount = uiState.searchUiState.itemTypeCount.loginCount,
                    aliasCount = uiState.searchUiState.itemTypeCount.aliasCount,
                    noteCount = uiState.searchUiState.itemTypeCount.noteCount,
                    creditCardCount = uiState.searchUiState.itemTypeCount.creditCardCount,
                    onItemTypeClick = { onEvent(HomeUiEvent.ItemTypeSelected(it)) }
                )
            }

            val showItemListHeader = remember(uiState) { shouldShowItemListHeader(uiState) }
            if (showItemListHeader) {
                val count = remember(uiState.homeListUiState.items) {
                    uiState.homeListUiState.items.map { it.items }.flatten().count()
                }
                ItemListHeader(
                    showSearchResults = uiState.searchUiState.inSearchMode &&
                        uiState.searchUiState.searchQuery.isNotEmpty(),
                    itemType = uiState.homeListUiState.searchFilterType,
                    itemCount = count.takeIf { !uiState.searchUiState.isProcessingSearch.value() },
                    sortingContent = {
                        SortingButton(
                            sortingType = uiState.homeListUiState.sortingType,
                            onSortingOptionsClick = { onEvent(HomeUiEvent.SortingOptionsClick) }
                        )
                    }
                )
            }

            val showRecentSearchHeader = remember(uiState) { shouldShowRecentSearchHeader(uiState) }
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

            ItemsList(
                modifier = Modifier.testTag("itemsList"),
                items = uiState.homeListUiState.items,
                selectedItemIds = uiState.homeListUiState.selectionState.selectedItems,
                isInSelectionMode = uiState.homeListUiState.selectionState.isInSelectMode,
                shares = uiState.homeListUiState.shares,
                isShareSelected = uiState.homeListUiState.selectedShare.isNotEmpty(),
                shouldScrollToTop = shouldScrollToTop,
                scrollableState = scrollableState,
                highlight = uiState.searchUiState.searchQuery,
                canLoadExternalImages = uiState.homeListUiState.canLoadExternalImages,
                onItemClick = { item ->
                    keyboardController?.hide()
                    if (uiState.homeListUiState.selectionState.isInSelectMode) {
                        onEvent(HomeUiEvent.SelectItem(item))
                    } else {
                        onEvent(HomeUiEvent.ItemClick(item))
                    }
                },
                onItemLongClick = { onEvent(HomeUiEvent.SelectItem(it)) },
                onItemMenuClick = { onEvent(HomeUiEvent.ItemMenuClick(it)) },
                isLoading = uiState.homeListUiState.isLoading,
                isProcessingSearch = uiState.searchUiState.isProcessingSearch,
                isRefreshing = uiState.homeListUiState.isRefreshing,
                onRefresh = { onEvent(HomeUiEvent.Refresh) },
                onScrollToTop = { onEvent(HomeUiEvent.ScrollToTop) },
                emptyContent = {
                    if (isTrashMode) {
                        EmptyTrashContent()
                    } else if (uiState.searchUiState.inSearchMode) {
                        EmptySearchResults()
                    } else {
                        HomeEmptyList(
                            modifier = Modifier.fillMaxHeight(),
                            onCreateLoginClick = {
                                val shareId = uiState.homeListUiState.selectedShare.map { it.id }
                                onEvent(HomeUiEvent.AddItemClick(shareId, ItemTypeUiState.Login))
                            },
                            onCreateAliasClick = {
                                val shareId = uiState.homeListUiState.selectedShare.map { it.id }
                                onEvent(HomeUiEvent.AddItemClick(shareId, ItemTypeUiState.Alias))
                            },
                            onCreateNoteClick = {
                                val shareId = uiState.homeListUiState.selectedShare.map { it.id }
                                onEvent(HomeUiEvent.AddItemClick(shareId, ItemTypeUiState.Note))
                            }
                        )
                    }
                },

                forceShowHeader = forceShowHeader,
                header = {
                    item {
                        OnBoardingTips(
                            onTrialInfoClick = { onEvent(HomeUiEvent.TrialInfoClick) },
                            onInviteClick = { onEvent(HomeUiEvent.InviteClick) }
                        )
                    }
                },
            )
        }
    }
}

private fun shouldShowRecentSearchHeader(uiState: HomeUiState) =
    uiState.homeListUiState.items.isNotEmpty() &&
        uiState.searchUiState.inSearchMode &&
        uiState.searchUiState.isInSuggestionsMode

private fun shouldShowItemListHeader(uiState: HomeUiState) =
    uiState.homeListUiState.items.isNotEmpty() &&
        uiState.homeListUiState.isLoading == IsLoadingState.NotLoading &&
        !uiState.searchUiState.isProcessingSearch.value() &&
        !uiState.searchUiState.isInSuggestionsMode

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HomeDrawerIcon(
    modifier: Modifier = Modifier,
    uiState: HomeUiState,
    onDrawerIconClick: () -> Unit,
    onStopSearch: () -> Unit
) {
    if (!uiState.searchUiState.inSearchMode) {
        when (val share = uiState.homeListUiState.selectedShare) {
            None -> {
                when (uiState.homeListUiState.homeVaultSelection) {
                    VaultSelectionOption.AllVaults -> {
                        AllVaultsIcon(
                            modifier = modifier,
                            size = 48,
                            onClick = onDrawerIconClick
                        )
                    }

                    VaultSelectionOption.Trash -> {
                        TrashVaultIcon(
                            modifier = modifier,
                            size = 48,
                            iconSize = 20,
                            onClick = onDrawerIconClick
                        )
                    }

                    else -> {} // This combination is not possible
                }
            }

            is Some -> {
                VaultIcon(
                    modifier = modifier.size(48.dp),
                    backgroundColor = share.value.color.toColor(true),
                    iconColor = share.value.color.toColor(),
                    icon = share.value.icon.toResource(),
                    onClick = onDrawerIconClick
                )
            }
        }
    } else {
        ArrowBackIconButton(modifier) { onStopSearch() }
    }
}

object HomeContentTestTag {
    const val DrawerIconTestTag = "drawerIcon"
}

