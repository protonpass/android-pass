package me.proton.pass.presentation.components.common.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.model.ItemUiModel

@Composable
internal fun ItemRowContents(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: Option<String>
) {
    when (val itemType = item.itemType) {
        is ItemType.Login -> LoginRow(
            modifier = modifier,
            item = item,
            itemType = itemType,
            highlight = highlight
        )
        is ItemType.Note -> NoteRow(
            modifier = modifier,
            item = item,
            itemType = itemType,
            highlight = highlight
        )
        is ItemType.Alias -> AliasRow(
            modifier = modifier,
            item = item,
            itemType = itemType,
            highlight = highlight
        )
        ItemType.Password -> {}
    }
}
