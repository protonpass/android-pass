package me.proton.pass.presentation.components.common.item

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefreshState
import me.proton.android.pass.ui.shared.DropDownAction
import me.proton.android.pass.ui.shared.ItemDropdownMenu
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.PassSwipeRefresh
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.components.previewproviders.ItemUiModelPreviewProvider
import me.proton.pass.presentation.uievents.IsRefreshingState

typealias OnItemClick = (ItemUiModel) -> Unit

data class ItemAction(
    val onSelect: (ItemUiModel) -> Unit,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val textColor: Color
)

@Composable
fun ItemsList(
    items: List<ItemUiModel>,
    @StringRes emptyListMessage: Int,
    modifier: Modifier = Modifier,
    itemActions: List<ItemAction> = emptyList(),
    onRefresh: () -> Unit,
    isRefreshing: IsRefreshingState,
    onItemClick: OnItemClick? = null
) {
    PassSwipeRefresh(
        modifier = modifier.fillMaxSize(),
        state = SwipeRefreshState(isRefreshing is IsRefreshingState.Refreshing),
        onRefresh = onRefresh
    ) {
        if (items.isNotEmpty()) {
            LazyColumn(modifier = modifier.fillMaxSize()) {
                items(items) { item ->
                    ItemRow(
                        item = item,
                        onItemClicked = onItemClick,
                        itemActions = itemActions
                    )
                }
            }
        } else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(id = emptyListMessage),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
internal fun ItemRow(
    item: ItemUiModel,
    itemActions: List<ItemAction> = emptyList(),
    onItemClicked: OnItemClick? = null
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClicked?.invoke(item) }
            .padding(end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemRowContents(
            item = item,
            modifier = Modifier.weight(1f)
        )
        ItemRowActions(
            expanded = expanded,
            setExpanded = setExpanded,
            actions = itemActions,
            item = item
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

@Composable
internal fun LoginRow(
    item: ItemUiModel,
    itemType: ItemType.Login,
    modifier: Modifier = Modifier
) {
    ItemRow(
        icon = me.proton.core.presentation.R.drawable.ic_proton_key,
        title = item.name,
        subtitle = itemType.username,
        modifier = modifier
    )
}

@Composable
internal fun NoteRow(
    item: ItemUiModel,
    itemType: ItemType.Note,
    modifier: Modifier = Modifier
) {
    val processedText = itemType.text.replace("\n", " ")
    ItemRow(
        icon = me.proton.core.presentation.R.drawable.ic_proton_note,
        title = item.name,
        subtitle = processedText,
        modifier = modifier
    )
}

@Composable
internal fun AliasRow(
    item: ItemUiModel,
    itemType: ItemType.Alias,
    modifier: Modifier = Modifier
) {
    ItemRow(
        icon = me.proton.core.presentation.R.drawable.ic_proton_alias,
        title = item.name,
        subtitle = itemType.aliasEmail,
        modifier = modifier
    )
}

@Composable
internal fun ItemRow(
    @DrawableRes icon: Int,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400,
                color = ProtonTheme.colors.textNorm,
                modifier = Modifier.padding(start = 20.dp),
                maxLines = 1
            )
        }
        Row(modifier = Modifier.padding(start = 44.dp, end = 20.dp)) {
            Text(
                text = subtitle,
                color = ProtonTheme.colors.textWeak,
                fontSize = 14.sp,
                fontWeight = FontWeight.W400,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ItemRowActions(
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

@Preview
@Composable
fun ItemRowPreview(@PreviewParameter(ItemUiModelPreviewProvider::class) item: ItemUiModel) {
    ProtonTheme {
        Surface {
            ItemRow(item = item)
        }
    }
}
