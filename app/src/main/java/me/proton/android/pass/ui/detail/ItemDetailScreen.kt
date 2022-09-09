package me.proton.android.pass.ui.detail

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.AlertDialog
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import me.proton.android.pass.R
import me.proton.android.pass.ui.detail.alias.AliasDetail
import me.proton.android.pass.ui.detail.login.LoginDetail
import me.proton.android.pass.ui.detail.note.NoteDetail
import me.proton.android.pass.ui.shared.ChevronBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.presentation.components.common.rememberFlowWithLifecycle

@ExperimentalComposeUiApi
@Composable
fun ItemDetailScreen(
    onUpClick: () -> Unit,
    shareId: String,
    itemId: String,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    onMovedToTrash: () -> Unit,
    viewModel: ItemDetailViewModel = hiltViewModel()
) {
    viewModel.setContent(shareId, itemId)

    val viewState by rememberFlowWithLifecycle(flow = viewModel.state).collectAsState(initial = viewModel.initialState)
    val itemToDelete = remember { mutableStateOf<Item?>(null) }

    if (viewState is ItemDetailViewModel.State.ItemSentToTrash) {
        onMovedToTrash()
        return
    }

    Scaffold(
        topBar = {
            ItemDetailTopBar(
                viewState = viewState,
                onUpClick = onUpClick,
                onEditClick = onEditClick,
                onDeleteClick = { itemToDelete.value = it }
            )
        }
    ) { padding ->
        Box {
            val itemName = when (val state = viewState) {
                is ItemDetailViewModel.State.Content -> state.model.name
                else -> ""
            }
            ItemDetailContent(viewState, modifier = Modifier.padding(padding))
            ConfirmSendToTrashDialog(
                itemState = itemToDelete,
                itemName = itemName,
                title = R.string.alert_confirm_item_send_to_trash_title,
                message = R.string.alert_confirm_item_send_to_trash_message,
                onConfirm = { viewModel.sendItemToTrash(it) }
            )
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun ItemDetailTopBar(
    viewState: ItemDetailViewModel.State,
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    onDeleteClick: (Item?) -> Unit
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val topBarContent = extractTopBarContent(viewState = viewState)

    ProtonTopAppBar(
        title = { TopBarTitleView(topBarContent.title) },
        navigationIcon = { ChevronBackIcon(onUpClick = onUpClick) },
        actions = {
            IconButton(
                onClick = { setExpanded(true) },
                modifier = Modifier.then(Modifier.size(24.dp))
            ) {
                Icon(
                    painterResource(R.drawable.ic_proton_three_dots_vertical),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { setExpanded(false) },
                properties = PopupProperties(),
                modifier = Modifier
                    .background(color = ProtonTheme.colors.backgroundNorm)
            ) {
                DropDownAction(
                    title = stringResource(R.string.action_edit_placeholder, topBarContent.itemTypeName),
                    icon = R.drawable.ic_proton_eraser
                ) {
                    setExpanded(false)

                    if (viewState is ItemDetailViewModel.State.Content) {
                        val item = viewState.model.item
                        onEditClick(item.shareId, item.id, item.itemType)
                    }
                }
                DropDownAction(
                    title = stringResource(R.string.action_move_to_trash),
                    textColor = ProtonTheme.colors.notificationError,
                    icon = R.drawable.ic_proton_trash,
                ) {
                    setExpanded(false)

                    if (viewState is ItemDetailViewModel.State.Content) {
                        val item = viewState.model.item
                        onDeleteClick(item)
                    }
                }
            }
        }
    )
}

@Composable
private fun DropDownAction(
    title: String,
    textColor: Color = ProtonTheme.colors.textNorm,
    @DrawableRes icon: Int,
    onClick: () -> Unit
) {
    DropdownMenuItem(onClick = onClick) {
        Row {
            Text(
                text = title,
                color = textColor,
                fontWeight = FontWeight.W400
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.padding(start = 16.dp))
        }
    }
}

@Composable
private fun ItemDetailContent(
    viewState: ItemDetailViewModel.State,
    modifier: Modifier = Modifier
) {
    when (viewState) {
        is ItemDetailViewModel.State.Content -> ItemDetail(viewState.model.item, modifier)
        is ItemDetailViewModel.State.Loading -> {}
        is ItemDetailViewModel.State.Error -> {}
    }
}

@Composable
private fun ItemDetail(
    item: Item,
    modifier: Modifier = Modifier
) {
    when (item.itemType) {
        is ItemType.Login -> LoginDetail(item, modifier)
        is ItemType.Note -> NoteDetail(item, modifier)
        is ItemType.Alias -> AliasDetail(item, modifier)
    }
}

private data class TopBarContent(
    val title: String,
    val itemTypeName: String
)

@Composable
private fun extractTopBarContent(viewState: ItemDetailViewModel.State): TopBarContent =
    when (viewState) {
        is ItemDetailViewModel.State.Content -> TopBarContent(
            title = viewState.model.name,
            itemTypeName = stringResource(viewState.model.item.itemType.toStringRes()).lowercase()
        )
        else -> TopBarContent(title = "", itemTypeName = "")
    }

@Composable
fun ConfirmSendToTrashDialog(
    itemState: MutableState<Item?>,
    @StringRes title: Int,
    @StringRes message: Int,
    itemName: String,
    onConfirm: (Item) -> Unit,
) {
    val item = itemState.value ?: return

    AlertDialog(
        onDismissRequest = { itemState.value = null },
        title = { Text(stringResource(title)) },
        text = { Text(stringResource(message, itemName)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(item)
                itemState.value = null
            }) {
                Text(text = stringResource(id = R.string.presentation_alert_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = { itemState.value = null }) {
                Text(text = stringResource(id = R.string.presentation_alert_cancel))
            }
        }
    )
}
