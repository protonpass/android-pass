package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.buttons.PassFloatingActionButton
import proton.pass.domain.ItemType

@Composable
internal fun SelectItemScreenContent(
    modifier: Modifier = Modifier,
    uiState: SelectItemUiState,
    packageName: String?,
    webDomain: String?,
    onItemClicked: (ItemUiModel, Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onEnterSearch: () -> Unit,
    onStopSearching: () -> Unit,
    onCreateLoginClicked: () -> Unit,
    onClose: () -> Unit
) {
    var showAssociateDialog by remember { mutableStateOf(false) }
    var itemClicked by remember { mutableStateOf<Option<ItemUiModel>>(None) }
    if (showAssociateDialog) {
        AssociateAutofillItemDialog(
            itemUiModel = itemClicked.value(),
            packageName = packageName,
            webDomain = webDomain,
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
                onClick = onCreateLoginClicked
            )
        },
        topBar = {
            SelectItemTopAppBar(
                searchQuery = uiState.searchUiState.searchQuery,
                inSearchMode = uiState.searchUiState.inSearchMode,
                onSearchQueryChange = onSearchQueryChange,
                onEnterSearch = onEnterSearch,
                onStopSearching = onStopSearching,
                onClose = onClose
            )
        }
    ) { padding ->
        SelectItemList(
            modifier = modifier.padding(padding),
            uiState = uiState,
            onItemClicked = {
                val item = it.itemType as? ItemType.Login ?: return@SelectItemList
                if (shouldAskForAssociation(item, packageName, webDomain)) {
                    itemClicked = it.toOption()
                    showAssociateDialog = true
                } else {
                    onItemClicked(it, false)
                }
            }
        )
    }
}

private fun shouldAskForAssociation(
    item: ItemType.Login,
    packageName: String?,
    webDomain: String?
): Boolean = !packageName.isNullOrBlank() && !item.packageNames.contains(packageName) ||
    !webDomain.isNullOrBlank() && !item.websites.contains(webDomain)
