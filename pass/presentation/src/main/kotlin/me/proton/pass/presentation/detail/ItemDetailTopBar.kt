package me.proton.pass.presentation.detail

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.android.pass.ui.shared.ArrowBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Option
import me.proton.pass.common.api.Some
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ItemType
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.item.DropDownAction
import me.proton.pass.presentation.components.common.item.ItemDropdownMenu
import me.proton.pass.presentation.detail.DetailSnackbarMessages.ItemNotLoadedError

@ExperimentalComposeUiApi
@Composable
internal fun ItemDetailTopBar(
    modifier: Modifier = Modifier,
    uiState: Option<ItemModelUiState>,
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    onDeleteClick: (Item) -> Unit,
    onSnackbarMessage: (DetailSnackbarMessages) -> Unit
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (title, itemTypeName) = when (uiState) {
        None -> "" to ""
        is Some -> uiState.value.name to stringResource(uiState.value.item.itemType.toStringRes()).lowercase()
    }
    ProtonTopAppBar(
        modifier = modifier,
        title = { TopBarTitleView(title = title) },
        navigationIcon = { ArrowBackIcon(onUpClick = onUpClick) },
        actions = {
            IconButton(
                onClick = { setExpanded(true) },
                modifier = Modifier.then(Modifier.size(24.dp))
            ) {
                Icon(
                    painterResource(me.proton.core.presentation.R.drawable.ic_proton_three_dots_vertical),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm
                )
            }

            ItemDropdownMenu(expanded = expanded, setExpanded = { setExpanded(false) }) {
                DropDownAction(
                    title = stringResource(
                        R.string.action_edit_placeholder,
                        itemTypeName
                    ),
                    icon = me.proton.core.presentation.R.drawable.ic_proton_eraser
                ) {
                    setExpanded(false)

                    when (uiState) {
                        None -> onSnackbarMessage(ItemNotLoadedError)
                        is Some -> {
                            val item = uiState.value.item
                            onEditClick(item.shareId, item.id, item.itemType)
                        }
                    }
                }
                DropDownAction(
                    title = stringResource(R.string.action_move_to_trash),
                    textColor = ProtonTheme.colors.notificationError,
                    icon = me.proton.core.presentation.R.drawable.ic_proton_trash
                ) {
                    setExpanded(false)

                    when (uiState) {
                        None -> onSnackbarMessage(ItemNotLoadedError)
                        is Some -> onDeleteClick(uiState.value.item)
                    }
                }
            }
        }
    )
}
