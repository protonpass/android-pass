package proton.android.pass.featureitemdetail.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import proton.android.pass.featureitemdetail.impl.alias.AliasDetail
import proton.android.pass.featureitemdetail.impl.login.LoginDetail
import proton.android.pass.featureitemdetail.impl.note.NoteDetail
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

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
