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

import androidx.annotation.VisibleForTesting
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
import proton.android.pass.autofill.extensions.isBrowser
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.common.AutofillConfirmMode
import proton.android.pass.autofill.ui.autofill.common.ConfirmAutofillDialog
import proton.android.pass.autofill.ui.autofill.navigation.SelectItemNavigation
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.buttons.PassFloatingActionButton
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.entity.PackageName

@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    packageInfo: PackageInfoUi,
    webDomain: String?,
    onItemClicked: (ItemUiModel, Boolean) -> Unit,
    onItemOptionsClicked: (ItemUiModel) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    onScrolledToTop: () -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit
) {
    val (showAssociateDialog, setShowAssociateDialog) = remember { mutableStateOf(false) }
    val (showWarning, setShowWarning) = remember { mutableStateOf<Option<AutofillConfirmMode>>(None) }
    val (itemClicked, setItemClicked) = remember { mutableStateOf<Option<ItemUiModel>>(None) }

    var willNeedToShowAssociateDialog by remember { mutableStateOf(false) }
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

    WarningDialog(
        showWarning = showWarning,
        setShowWarning = setShowWarning,
        willNeedToShowAssociateDialog = willNeedToShowAssociateDialog,
        setShowAssociateDialog = setShowAssociateDialog,
        itemClicked = itemClicked,
        onItemClicked = onItemClicked
    )

    AssociateDialog(
        showAssociateDialog = showAssociateDialog,
        setShowAssociateDialog = setShowAssociateDialog,
        itemClicked = itemClicked,
        setItemClicked = setItemClicked,
        onItemClicked = onItemClicked,
    )

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
            onItemClicked = { item ->
                when (val contents = item.contents) {
                    is ItemContents.Login -> {
                        val askForAssociation = shouldAskForAssociation(
                            item = contents,
                            packageName = packageInfo.toPackageInfo().packageName,
                            webDomain = webDomain
                        )

                        when (uiState.confirmMode) {
                            is Some -> {
                                setItemClicked(item.some())
                                willNeedToShowAssociateDialog = askForAssociation
                                setShowWarning(uiState.confirmMode)
                            }
                            None -> {
                                if (askForAssociation) {
                                    setItemClicked(item.some())
                                    setShowAssociateDialog(true)
                                } else {
                                    onItemClicked(item, false)
                                }
                            }
                        }
                    }

                    is ItemContents.CreditCard -> when (uiState.confirmMode) {
                        None -> onItemClicked(item, false)
                        is Some -> {
                            setItemClicked(item.some())

                            // Credit cards are not associated
                            willNeedToShowAssociateDialog = false
                            setShowWarning(uiState.confirmMode)
                        }
                    }
                    else -> throw IllegalStateException("Unhandled item type")
                }
            },
            onNavigate = onNavigate
        )
    }
}

@Composable
private fun WarningDialog(
    modifier: Modifier = Modifier,
    showWarning: Option<AutofillConfirmMode>,
    setShowWarning: (Option<AutofillConfirmMode>) -> Unit,
    willNeedToShowAssociateDialog: Boolean,
    setShowAssociateDialog: (Boolean) -> Unit,
    itemClicked: Option<ItemUiModel>,
    onItemClicked: (ItemUiModel, Boolean) -> Unit
) {
    when (showWarning) {
        None -> {}
        is Some -> {
            ConfirmAutofillDialog(
                modifier = modifier,
                mode = showWarning.value,
                onClose = { setShowWarning(None) },
                onConfirm = {
                    if (willNeedToShowAssociateDialog) {
                        setShowAssociateDialog(true)
                    } else {
                        itemClicked.value()?.let {
                            onItemClicked(it, false)
                        }
                    }
                    setShowWarning(None)
                }
            )
        }
    }
}

@Composable
private fun AssociateDialog(
    modifier: Modifier = Modifier,
    showAssociateDialog: Boolean,
    setShowAssociateDialog: (Boolean) -> Unit,
    itemClicked: Option<ItemUiModel>,
    setItemClicked: (Option<ItemUiModel>) -> Unit,
    onItemClicked: (ItemUiModel, Boolean) -> Unit
) {
    if (!showAssociateDialog) return
    AssociateAutofillItemDialog(
        modifier = modifier,
        itemUiModel = itemClicked.value(),
        onAssociateAndAutofill = {
            onItemClicked(it, true)
            setShowAssociateDialog(false)
            setItemClicked(None)
        },
        onAutofill = {
            onItemClicked(it, false)
            setShowAssociateDialog(false)
            setItemClicked(None)
        },
        onDismiss = {
            setShowAssociateDialog(false)
            setItemClicked(None)
        }
    )
}

@VisibleForTesting
fun shouldAskForAssociation(
    item: ItemContents.Login,
    packageName: PackageName,
    webDomain: String?
): Boolean = when {
    // If the package name is not a browser and the package name is already associated with the item
    // do not ask for association
    !packageName.isBrowser() && item.packageInfoSet.map {
        it.packageName
    }.contains(packageName) -> false

    // If the package name is not a browser and there is no web domain, ask for association
    !packageName.isBrowser() && webDomain.isNullOrBlank() -> true

    // From then on we are sure that there is a webDomain
    // Check if is already there, and if it is, do not ask for association
    !webDomain.isNullOrBlank() && item.urls.contains(webDomain) -> false

    else -> true
}
