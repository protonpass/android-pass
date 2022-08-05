package me.proton.android.pass.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
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

@Composable
fun ItemsList(
    items: List<ItemUiModel>,
    modifier: Modifier = Modifier,
    onItemClick: OnItemClick,
    onEditItemClick: OnItemClick,
    onDeleteItemClicked: (ItemUiModel) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(items) { item ->
            ItemRow(
                item = item,
                onItemClicked = onItemClick,
                onEditClicked = onEditItemClick,
                onDeleteClicked = onDeleteItemClicked
            )
        }
    }
}

@Composable
internal fun ItemRow(
    item: ItemUiModel,
    onItemClicked: OnItemClick,
    onEditClicked: OnItemClick,
    onDeleteClicked: (ItemUiModel) -> Unit
) {
    when (val itemType = item.itemType) {
        is ItemType.Login -> LoginRow(
            item = item,
            itemType = itemType,
            onItemClicked = onItemClicked,
            onEditClicked = onEditClicked,
            onDeleteClicked = onDeleteClicked
        )
        is ItemType.Note -> NoteRow(
            item = item,
            itemType = itemType,
            onItemClicked = onItemClicked,
            onEditClicked = onEditClicked,
            onDeleteClicked = onDeleteClicked
        )
        is ItemType.Alias -> AliasRow(
            item = item,
            onItemClicked = onItemClicked,
            onEditClicked = onEditClicked,
            onDeleteClicked = onDeleteClicked
        )
    }
}

@Composable
internal fun LoginRow(
    item: ItemUiModel,
    itemType: ItemType.Login,
    onItemClicked: OnItemClick,
    onEditClicked: OnItemClick,
    onDeleteClicked: (ItemUiModel) -> Unit
) {
    ItemRow(
        icon = R.drawable.ic_proton_key,
        title = item.name,
        subtitle = itemType.username,
        onItemClicked = { onItemClicked(item.shareId, item.id) },
        onEditClicked = { onEditClicked(item.shareId, item.id) },
        onDeleteClicked = { onDeleteClicked(item) }
    )
}

@Composable
internal fun NoteRow(
    item: ItemUiModel,
    itemType: ItemType.Note,
    onItemClicked: OnItemClick,
    onEditClicked: OnItemClick,
    onDeleteClicked: (ItemUiModel) -> Unit
) {
    ItemRow(
        icon = R.drawable.ic_proton_note,
        title = item.name,
        subtitle = itemType.text.take(10),
        onItemClicked = { onItemClicked(item.shareId, item.id) },
        onEditClicked = { onEditClicked(item.shareId, item.id) },
        onDeleteClicked = { onDeleteClicked(item) }
    )
}

@Composable
internal fun AliasRow(
    item: ItemUiModel,
    onItemClicked: OnItemClick,
    onEditClicked: OnItemClick,
    onDeleteClicked: (ItemUiModel) -> Unit
) {
    ItemRow(
        icon = R.drawable.ic_proton_alias,
        title = item.name,
        subtitle = "", // TODO: Extract alias
        onItemClicked = { onItemClicked(item.shareId, item.id) },
        onEditClicked = { onEditClicked(item.shareId, item.id) },
        onDeleteClicked = { onDeleteClicked(item) }
    )
}

@Composable
internal fun ItemRow(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    onItemClicked: () -> Unit,
    onEditClicked: () -> Unit,
    onDeleteClicked: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clickable { onItemClicked() }
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
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
                modifier = Modifier.padding(start = 20.dp)
            )
            Spacer(Modifier.weight(1f))
            Box {
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.then(Modifier.size(24.dp))
                ) {
                    Icon(
                        ImageVector.vectorResource(R.drawable.ic_three_dots_vertical_24),
                        contentDescription = stringResource(id = R.string.action_delete),
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    properties = PopupProperties()
                ) {
                    DropdownMenuItem(onClick = {
                        onEditClicked()
                        expanded = false
                    }) {
                        Text(text = stringResource(id = R.string.action_edit))
                    }
                    DropdownMenuItem(onClick = {
                        onDeleteClicked()
                        expanded = false
                    }) {
                        Text(text = stringResource(id = R.string.action_delete))
                    }
                }
            }
        }
        Row(modifier = Modifier.padding(start = 44.dp, end = 20.dp)) {
            Text(
                text = subtitle,
                color = ProtonTheme.colors.textWeak,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
            )
        }
    }
}
