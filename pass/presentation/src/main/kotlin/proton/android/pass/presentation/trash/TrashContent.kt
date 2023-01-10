package proton.android.pass.presentation.trash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.dialogs.ConfirmItemDeletionDialog
import me.proton.pass.presentation.R

@Composable
fun TrashContent(
    modifier: Modifier,
    uiState: TrashUiState,
    onTopBarMenuClick: () -> Unit,
    onItemMenuClick: (ItemUiModel) -> Unit,
    onDeleteItem: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit,
    onDrawerIconClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TrashTopBar(
                showActions = uiState.items.isNotEmpty(),
                onDrawerIconClick = onDrawerIconClick,
                onMoreOptionsClick = onTopBarMenuClick
            )
        }
    ) { contentPadding ->
        Box(modifier = modifier.padding(contentPadding)) {
            var itemToDelete by remember { mutableStateOf<ItemUiModel?>(null) }
            TrashItemList(
                items = uiState.items,
                isRefreshing = uiState.isRefreshing,
                isLoading = uiState.isLoading,
                onItemMenuClick = onItemMenuClick,
                onRefresh = onRefresh
            )
            ConfirmItemDeletionDialog(
                state = itemToDelete,
                onDismiss = { itemToDelete = null },
                title = R.string.alert_confirm_item_deletion_title,
                message = R.string.alert_confirm_item_deletion_message,
                onConfirm = onDeleteItem
            )
        }
    }
}
