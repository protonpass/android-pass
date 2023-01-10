package proton.android.pass.presentation.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import proton.android.pass.presentation.detail.alias.AliasDetail
import proton.android.pass.presentation.detail.login.LoginDetail
import proton.android.pass.presentation.detail.note.NoteDetail

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ItemDetailContent(
    modifier: Modifier = Modifier,
    uiState: ItemDetailScreenUiState,
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.model != null) {
            val item = uiState.model.item
            val topBar: @Composable () -> Unit = {
                ItemDetailTopBar(
                    title = uiState.model.name,
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
