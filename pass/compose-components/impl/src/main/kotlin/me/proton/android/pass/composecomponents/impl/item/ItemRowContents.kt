package me.proton.android.pass.composecomponents.impl.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.pass.domain.ItemType

@Composable
internal fun ItemRowContents(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String
) {
    when (item.itemType) {
        is ItemType.Login -> LoginRow(
            modifier = modifier,
            item = item,
            highlight = highlight
        )
        is ItemType.Note -> NoteRow(
            modifier = modifier,
            item = item,
            highlight = highlight
        )
        is ItemType.Alias -> AliasRow(
            modifier = modifier,
            item = item,
            highlight = highlight
        )
        ItemType.Password -> {}
    }
}
