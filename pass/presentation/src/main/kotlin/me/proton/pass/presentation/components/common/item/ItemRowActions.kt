package me.proton.pass.presentation.components.common.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.model.ItemUiModel

@Composable
internal fun ItemRowActions(
    expanded: Boolean,
    setExpanded: (Boolean) -> Unit,
    actions: List<ItemAction>,
    item: ItemUiModel
) {
    if (actions.isEmpty()) return

    Box {
        IconButton(
            onClick = { setExpanded(true) },
            modifier = Modifier.then(Modifier.size(24.dp))
        ) {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_three_dots_vertical_24),
                contentDescription = stringResource(id = R.string.action_delete)
            )
        }

        ItemDropdownMenu(
            modifier = Modifier,
            expanded = expanded,
            setExpanded = { setExpanded(false) }
        ) {
            actions.forEach {
                DropDownAction(
                    title = stringResource(
                        it.title,
                        stringResource(item.itemType.toStringRes()).lowercase()
                    ),
                    textColor = it.textColor,
                    icon = it.icon
                ) {
                    setExpanded(false)
                    it.onSelect(item)
                }
            }
        }
    }
}
