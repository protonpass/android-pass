package me.proton.android.pass.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import me.proton.android.pass.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.presentation.components.model.ItemUiModel

data class ItemExtraAction(
    val onSelect: (ItemUiModel) -> Unit,
    @StringRes val title: Int
)

@Composable
fun ItemsList(
    items: List<ItemUiModel>,
    modifier: Modifier = Modifier,
    itemActions: List<ItemExtraAction> = emptyList(),
    onItemClick: OnItemClick? = null,
) {
    LazyColumn(modifier = modifier) {
        items(items) { item ->
            ItemRow(
                item = item,
                onItemClicked = onItemClick,
                itemActions = itemActions
            )
        }
    }
}

@Composable
internal fun ItemRow(
    item: ItemUiModel,
    itemActions: List<ItemExtraAction> = emptyList(),
    onItemClicked: OnItemClick? = null,
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClicked?.invoke(item.shareId, item.id) }
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemRowContents(
            item = item,
            modifier = Modifier.weight(1f)
        )
        ItemRowExtraOptions(
            expanded = expanded,
            setExpanded = setExpanded,
            actions = itemActions,
            item = item,
        )
    }
}

@Composable
internal fun ItemRowContents(
    item: ItemUiModel,
    modifier: Modifier = Modifier
) {
    when (val itemType = item.itemType) {
        is ItemType.Login -> LoginRow(
            item = item,
            itemType = itemType,
            modifier = modifier,
        )
        is ItemType.Note -> NoteRow(
            item = item,
            itemType = itemType,
            modifier = modifier,
        )
        is ItemType.Alias -> AliasRow(
            item = item,
            itemType = itemType,
            modifier = modifier,
        )
    }
}

@Composable
internal fun LoginRow(
    item: ItemUiModel,
    itemType: ItemType.Login,
    modifier: Modifier = Modifier
) {
    ItemRow(
        icon = R.drawable.ic_proton_key,
        title = item.name,
        subtitle = itemType.username,
        modifier = modifier,
    )
}

@Composable
internal fun NoteRow(
    item: ItemUiModel,
    itemType: ItemType.Note,
    modifier: Modifier = Modifier
) {
    ItemRow(
        icon = R.drawable.ic_proton_note,
        title = item.name,
        subtitle = itemType.text.take(10),
        modifier = modifier,
    )
}

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
        modifier = modifier,
    )
}

@Composable
internal fun ItemRow(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm,
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                color = ProtonTheme.colors.textNorm,
                modifier = Modifier.padding(start = 20.dp),
                maxLines = 1,
            )
        }
        Row(modifier = Modifier.padding(start = 44.dp, end = 20.dp)) {
            Text(
                text = subtitle,
                color = ProtonTheme.colors.textWeak,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ItemRowExtraOptions(
    expanded: Boolean,
    setExpanded: (Boolean) -> Unit,
    actions: List<ItemExtraAction>,
    item: ItemUiModel,
) {
    if (actions.isEmpty()) return

    Box {
        IconButton(
            onClick = { setExpanded(true) },
            modifier = Modifier.then(Modifier.size(24.dp))
        ) {
            Icon(
                ImageVector.vectorResource(R.drawable.ic_three_dots_vertical_24),
                contentDescription = stringResource(id = R.string.action_delete),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { setExpanded(false) },
            properties = PopupProperties()
        ) {
            actions.forEach {
                DropdownMenuItem(onClick = {
                    it.onSelect(item)
                    setExpanded(false)
                }) {
                    Text(text = stringResource(id = it.title))
                }
            }
        }
    }
}
