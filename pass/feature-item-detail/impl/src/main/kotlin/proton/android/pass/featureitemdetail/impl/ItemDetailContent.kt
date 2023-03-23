package proton.android.pass.featureitemdetail.impl

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.featureitemdetail.impl.alias.AliasDetail
import proton.android.pass.featureitemdetail.impl.login.LoginDetail
import proton.android.pass.featureitemdetail.impl.note.NoteDetail
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId

@Composable
fun ItemDetailContent(
    modifier: Modifier = Modifier,
    uiState: ItemDetailScreenUiState,
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    onMigrateClick: (ShareId, ItemId) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.model != null && uiState.moreInfoUiState != null) {
            val item = uiState.model.item
            when (item.itemType) {
                is ItemType.Login -> LoginDetail(
                    item = item,
                    moreInfoUiState = uiState.moreInfoUiState,
                    onUpClick = onUpClick,
                    onEditClick = onEditClick,
                    onMigrateClick = onMigrateClick
                )
                is ItemType.Note -> NoteDetail(
                    item = item,
                    moreInfoUiState = uiState.moreInfoUiState,
                    onUpClick = onUpClick,
                    onEditClick = onEditClick,
                    onMigrateClick = onMigrateClick
                )
                is ItemType.Alias -> AliasDetail(
                    item = item,
                    moreInfoUiState = uiState.moreInfoUiState,
                    onUpClick = onUpClick,
                    onEditClick = onEditClick,
                    onMigrateClick = onMigrateClick
                )
                ItemType.Password -> {}
            }
        }
    }
}
