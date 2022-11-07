package me.proton.pass.presentation.components.common.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.presentation.R
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.model.ItemUiModel

@Composable
internal fun NoteRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    itemType: ItemType.Note,
    highlight: Option<String>
) {
    val processedText = itemType.text.replace("\n", " ")
    ItemRow(
        icon = R.drawable.ic_proton_note,
        title = item.name.highlight(highlight),
        subtitle = processedText.highlight(highlight),
        modifier = modifier
    )
}
