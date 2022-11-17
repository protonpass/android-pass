package me.proton.android.pass.ui.trash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.proton.android.pass.ui.shared.ConfirmItemDeletionDialog
import me.proton.pass.presentation.components.model.ItemUiModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TrashContent(
    modifier: Modifier,
    uiState: TrashUiState,
    onRestoreItem: (ItemUiModel) -> Unit,
    onDeleteItem: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit,
    onClearTrash: () -> Unit,
    onDrawerIconClick: () -> Unit
) {

    var showClearTrashDialog by rememberSaveable { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TrashTopBar(
                onDrawerIconClick = onDrawerIconClick,
                onClearTrashClick = { showClearTrashDialog = true }
            )
        }
    ) { contentPadding ->
        Box(modifier = modifier.padding(contentPadding)) {
            var itemToDelete by remember { mutableStateOf<ItemUiModel?>(null) }
            TrashItemList(
                items = uiState.items,
                isRefreshing = uiState.isRefreshing,
                isLoading = uiState.isLoading,
                onRestoreClicked = { onRestoreItem(it) },
                onDeleteItemClicked = { itemToDelete = it },
                onRefresh = onRefresh
            )

            ConfirmClearTrashDialog(
                show = showClearTrashDialog,
                onDismiss = { showClearTrashDialog = false },
                onConfirm = onClearTrash
            )
            ConfirmItemDeletionDialog(
                state = itemToDelete,
                onDismiss = { itemToDelete = null },
                title = me.proton.pass.presentation.R.string.alert_confirm_item_deletion_title,
                message = me.proton.pass.presentation.R.string.alert_confirm_item_deletion_message,
                onConfirm = onDeleteItem
            )
        }
    }
}
