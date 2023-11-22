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

package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.navigation.SelectItemNavigation
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.buttons.PassFloatingActionButton
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.domain.ItemContents

@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    packageInfo: PackageInfoUi?,
    webDomain: String?,
    onItemClicked: (ItemUiModel, Boolean) -> Unit,
    onItemOptionsClicked: (ItemUiModel) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    onScrolledToTop: () -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit
) {
    var showAssociateDialog by remember { mutableStateOf(false) }
    var itemClicked by remember { mutableStateOf<Option<ItemUiModel>>(None) }
    val verticalScroll = rememberLazyListState()
    var showFab by remember { mutableStateOf(true) }

    LaunchedEffect(verticalScroll) {
        var prev = 0
        snapshotFlow { verticalScroll.firstVisibleItemIndex }
            .collect {
                showFab = it <= prev
                prev = it
            }
    }

    if (showAssociateDialog) {
        AssociateAutofillItemDialog(
            itemUiModel = itemClicked.value(),
            onAssociateAndAutofill = {
                onItemClicked(it, true)
                showAssociateDialog = false
                itemClicked = None
            },
            onAutofill = {
                onItemClicked(it, false)
                showAssociateDialog = false
                itemClicked = None
            },
            onDismiss = {
                showAssociateDialog = false
                itemClicked = None
            }
        )
    }
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            PassFloatingActionButton(
                visible = showFab,
                onClick = { onNavigate(SelectItemNavigation.AddItem) }
            )
        },
        topBar = {
            val placeholder = when (uiState.searchUiState.searchInMode) {
                SearchInMode.AllVaults -> stringResource(id = R.string.topbar_search_query)
                SearchInMode.OldestVaults -> stringResource(id = R.string.topbar_search_query_oldest_vaults)
                SearchInMode.Uninitialized -> stringResource(id = R.string.topbar_search_query_uninitialized)
            }

            SearchTopBar(
                placeholderText = placeholder,
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                onSearchQueryChange = onSearchQueryChange,
                onStopSearch = onStopSearching,
                onEnterSearch = onEnterSearch,
                drawerIcon = {
                    BackArrowCircleIconButton(
                        color = PassTheme.colors.loginInteractionNorm,
                        backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                        onUpClick = {
                            if (uiState.searchUiState.inSearchMode) {
                                onStopSearching()
                            } else {
                                onNavigate(SelectItemNavigation.Cancel)
                            }
                        }
                    )
                }
            )
        }
    ) { padding ->
        SelectItemList(
            modifier = Modifier.padding(padding),
            uiState = uiState,
            scrollState = verticalScroll,
            onScrolledToTop = onScrolledToTop,
            onItemOptionsClicked = onItemOptionsClicked,
            onItemClicked = {
                when (val contents = it.contents) {
                    is ItemContents.Login -> {
                        val askForAssociation = shouldAskForAssociation(
                            contents,
                            packageInfo?.packageName,
                            webDomain
                        )
                        if (askForAssociation) {
                            itemClicked = it.toOption()
                            showAssociateDialog = true
                        } else {
                            onItemClicked(it, false)
                        }
                    }

                    is ItemContents.CreditCard -> onItemClicked(it, false)
                    else -> throw IllegalStateException("Unhandled item type")
                }

            },
            onNavigate = onNavigate
        )
    }
}

private fun shouldAskForAssociation(
    item: ItemContents.Login,
    packageName: String?,
    webDomain: String?
): Boolean = !packageName.isNullOrBlank() &&
    !item.packageInfoSet.map { it.packageName.value }.contains(packageName) ||
    !webDomain.isNullOrBlank() && !item.urls.contains(webDomain)
