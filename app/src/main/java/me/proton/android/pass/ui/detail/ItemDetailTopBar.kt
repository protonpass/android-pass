package me.proton.android.pass.ui.detail

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
import me.proton.android.pass.ui.detail.DetailSnackbarMessages.ItemNotLoadedError
import me.proton.android.pass.ui.shared.ChevronBackIcon
import me.proton.android.pass.ui.shared.DropDownAction
import me.proton.android.pass.ui.shared.ItemDropdownMenu
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.common.api.None
import me.proton.core.pass.common.api.Option
import me.proton.core.pass.common.api.Some
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.presentation.R

@ExperimentalComposeUiApi
@Composable
internal fun ItemDetailTopBar(
    modifier: Modifier = Modifier,
    uiState: Option<ItemModelUiState>,
    onUpClick: () -> Unit,
    onEditClick: (ShareId, ItemId, ItemType) -> Unit,
    onDeleteClick: (Item?) -> Unit,
    onSnackbarMessage: (DetailSnackbarMessages) -> Unit
) {
    val (expanded, setExpanded) = remember { mutableStateOf(false) }
    val (title, itemTypeName) = when (uiState) {
        None -> "" to ""
        is Some -> uiState.value.name to stringResource(uiState.value.item.itemType.toStringRes()).lowercase()
    }
    ProtonTopAppBar(
        modifier = modifier,
        title = { TopBarTitleView(title) },
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

            ItemDropdownMenu(expanded = expanded, setExpanded = { setExpanded(false) }) {
                DropDownAction(
                    title = stringResource(
                        me.proton.android.pass.R.string.action_edit_placeholder,
                        itemTypeName
                    ),
                    icon = R.drawable.ic_proton_eraser
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
                    title = stringResource(me.proton.android.pass.R.string.action_move_to_trash),
                    textColor = ProtonTheme.colors.notificationError,
                    icon = R.drawable.ic_proton_trash
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
