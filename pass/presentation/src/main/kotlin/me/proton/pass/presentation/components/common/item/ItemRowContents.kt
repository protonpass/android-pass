package me.proton.pass.presentation.components.common.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.model.ItemUiModel

@Composable
internal fun ItemRowContents(
    item: ItemUiModel,
    modifier: Modifier = Modifier
) {
    when (val itemType = item.itemType) {
        is ItemType.Login -> LoginRow(
            item = item,
            itemType = itemType,
            modifier = modifier
        )
        is ItemType.Note -> NoteRow(
            item = item,
            itemType = itemType,
            modifier = modifier
        )
        is ItemType.Alias -> AliasRow(
            item = item,
            itemType = itemType,
            modifier = modifier
        )
        ItemType.Password -> {}
    }
}
