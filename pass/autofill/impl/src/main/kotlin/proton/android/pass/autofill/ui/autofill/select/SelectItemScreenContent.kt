package proton.android.pass.autofill.ui.autofill.select

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
import proton.android.pass.composecomponents.impl.topbar.SearchTopBar
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.pass.domain.ItemType

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
            val placeholder = when (uiState.searchUiState.searchInMode) {
                SearchInMode.AllVaults -> stringResource(id = R.string.topbar_search_query)
                SearchInMode.PrimaryVault -> stringResource(id = R.string.topbar_search_query_primary_vault)
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
            onScrolledToTop = onScrolledToTop,
            onItemOptionsClicked = onItemOptionsClicked,
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

private fun shouldAskForAssociation(
    item: ItemType.Login,
    packageName: String?,
    webDomain: String?
): Boolean = !packageName.isNullOrBlank() &&
    !item.packageInfoSet.map { it.packageName.value }.contains(packageName) ||
    !webDomain.isNullOrBlank() && !item.websites.contains(webDomain)
