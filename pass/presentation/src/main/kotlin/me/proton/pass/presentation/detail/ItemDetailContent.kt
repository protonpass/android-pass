package me.proton.pass.presentation.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import me.proton.pass.common.api.Some
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
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
    onEditClick: (ShareId, ItemId, ItemType) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.isLoading == IsLoadingState.Loading) {
            LoadingDialog()
        }

        if (uiState.model is Some) {
            val item = uiState.model.value.item
            val itemName = uiState.model.value.name
            val topBar: @Composable () -> Unit = {
                ItemDetailTopBar(
                    title = itemName,
                    onUpClick = onUpClick,
                    onEditClick = { onEditClick(item.shareId, item.id, item.itemType) }
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
        }
    }
}
