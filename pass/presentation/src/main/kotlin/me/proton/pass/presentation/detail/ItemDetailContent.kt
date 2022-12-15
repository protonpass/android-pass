package me.proton.pass.presentation.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import me.proton.pass.common.api.Some
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.LoadingDialog
import me.proton.pass.presentation.detail.alias.AliasDetail
import me.proton.pass.presentation.detail.login.LoginDetail
import me.proton.pass.presentation.detail.note.NoteDetail
import me.proton.pass.presentation.uievents.IsLoadingState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ItemDetailContent(
    modifier: Modifier = Modifier,
    uiState: ItemDetailScreenUiState,
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    onMoveToTrash: (Item) -> Unit,
    onEmitSnackbarMessage: (DetailSnackbarMessages) -> Unit
) {
    val itemToDelete = remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading == IsLoadingState.Loading) {
            LoadingDialog()
        }

        if (uiState.model is Some) {
            val item = uiState.model.value.item
            val topBar: @Composable () -> Unit = {
                ItemDetailTopBar(
                    uiState = uiState.model,
                    onUpClick = onUpClick,
                    onEditClick = onEditClick,
                    onDeleteClick = { itemToDelete.value = true },
                    onSnackbarMessage = onEmitSnackbarMessage
                )
            }
            when (item.itemType) {
                is ItemType.Login -> LoginDetail(
                    topBar = topBar,
                    item = item
                )
                is ItemType.Note -> NoteDetail(
                    topBar = topBar,
                    item = item
                )
                is ItemType.Alias -> AliasDetail(
                    topBar = topBar,
                    item = item
                )
                ItemType.Password -> {}
            }

            if (itemToDelete.value) {
                ConfirmSendToTrashDialog(
                    item = item,
                    itemName = uiState.model.value.name,
                    title = R.string.alert_confirm_item_send_to_trash_title,
                    message = R.string.alert_confirm_item_send_to_trash_message,
                    onConfirm = onMoveToTrash,
                    onDismiss = { itemToDelete.value = false }
                )
            }
        }
    }
}
