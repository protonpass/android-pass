package me.proton.pass.presentation.components.common.item

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.core.presentation.R
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.model.ItemUiModel

@Composable
internal fun AliasRow(
    item: ItemUiModel,
    itemType: ItemType.Alias,
    modifier: Modifier = Modifier
) {
    ItemRow(
        icon = R.drawable.ic_proton_alias,
        title = item.name,
        subtitle = itemType.aliasEmail,
        modifier = modifier
    )
}
