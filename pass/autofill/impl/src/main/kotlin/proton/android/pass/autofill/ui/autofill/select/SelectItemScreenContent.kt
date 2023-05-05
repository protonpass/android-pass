package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import proton.android.pass.composecomponents.impl.item.header.ItemListHeader
import proton.android.pass.composecomponents.impl.item.header.SortingButton
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.pass.domain.ItemType

@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    packageInfo: PackageInfoUi?,
    webDomain: String?,
    onItemClicked: (ItemUiModel, Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    onScrollToTop: () -> Unit,
    onNavigate: (SelectItemNavigation) -> Unit
) {
    var showAssociateDialog by remember { mutableStateOf(false) }
    var itemClicked by remember { mutableStateOf<Option<ItemUiModel>>(None) }
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
                onClick = { onNavigate(SelectItemNavigation.AddItem) }
            )
        },
        topBar = {
            SearchTopBar(
                placeholderText = stringResource(id = R.string.topbar_search_query),
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
        Column(
            modifier = Modifier.padding(padding)
        ) {
            val count = remember(uiState.listUiState.items) {
                uiState.listUiState.items.items.map { it.items }.flatten().count() +
                    uiState.listUiState.items.suggestions.count()
            }
            if (shouldShowItemListHeader(uiState)) {
                ItemListHeader(
                    showSearchResults = uiState.searchUiState.inSearchMode &&
                        uiState.searchUiState.searchQuery.isNotEmpty(),
                    itemCount = count.takeIf { !uiState.searchUiState.isProcessingSearch.value() },
                    sortingContent = {
                        SortingButton(
                            sortingType = uiState.listUiState.sortingType,
                            onSortingOptionsClick = {
                                onNavigate(SelectItemNavigation.SortingBottomsheet(uiState.listUiState.sortingType))
                            }
                        )
                    }
                )
            }

            SelectItemList(
                uiState = uiState,
                onScrollToTop = onScrollToTop,
                onItemClicked = {
                    val item = it.itemType as? ItemType.Login ?: return@SelectItemList
                    if (shouldAskForAssociation(item, packageInfo?.packageName, webDomain)) {
                        itemClicked = it.toOption()
                        showAssociateDialog = true
                    } else {
                        onItemClicked(it, false)
                    }
                },
                onNavigate = onNavigate
            )
        }
    }
}

private fun shouldAskForAssociation(
    item: ItemType.Login,
    packageName: String?,
    webDomain: String?
): Boolean = !packageName.isNullOrBlank() &&
    !item.packageInfoSet.map { it.packageName.value }.contains(packageName) ||
    !webDomain.isNullOrBlank() && !item.websites.contains(webDomain)

private fun shouldShowItemListHeader(uiState: SelectItemUiState) =
    uiState.listUiState.items.items.isNotEmpty() &&
        uiState.listUiState.isLoading == IsLoadingState.NotLoading &&
        !uiState.searchUiState.isProcessingSearch.value()
