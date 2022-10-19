package me.proton.android.pass.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import me.proton.android.pass.R
import me.proton.android.pass.ui.detail.alias.AliasDetail
import me.proton.android.pass.ui.detail.login.LoginDetail
import me.proton.android.pass.ui.detail.note.NoteDetail
import me.proton.android.pass.ui.shared.LoadingDialog
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.uievents.IsLoadingState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ItemDetailContent(
    modifier: Modifier = Modifier,
    uiState: ItemDetailScreenUiState,
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    onMoveToTrash: (Item) -> Unit,
    onSnackbarMessage: (DetailSnackbarMessages) -> Unit
) {
    val itemToDelete = remember { mutableStateOf<Item?>(null) }

    Scaffold(
        topBar = {
            ItemDetailTopBar(
                uiState = uiState.model,
                onUpClick = onUpClick,
                onEditClick = onEditClick,
                onDeleteClick = { itemToDelete.value = it },
                onSnackbarMessage = onSnackbarMessage
            )
        }
    ) { padding ->
        if (uiState.isLoading == IsLoadingState.Loading) {
            LoadingDialog()
        }
        Box(modifier = modifier.padding(padding)) {
            when (uiState.model) {
                None -> Text("No item")
                is Some -> {
                    val item = uiState.model.value.item
                    when (item.itemType) {
                        is ItemType.Login -> LoginDetail(item, modifier)
                        is ItemType.Note -> NoteDetail(item, modifier)
                        is ItemType.Alias -> AliasDetail(item, modifier)
                        ItemType.Password -> {}
                    }
                    ConfirmSendToTrashDialog(
                        itemState = itemToDelete,
                        itemName = uiState.model.value.name,
                        title = R.string.alert_confirm_item_send_to_trash_title,
                        message = R.string.alert_confirm_item_send_to_trash_message,
                        onConfirm = onMoveToTrash
                    )
                }
            }
        }
    }
}
