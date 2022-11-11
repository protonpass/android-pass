package me.proton.android.pass.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.android.pass.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.common.api.Option
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.common.item.ItemAction
import me.proton.pass.presentation.components.common.item.ItemsList
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsRefreshingState

@Composable
internal fun Home(
    items: List<ItemUiModel>,
    highlight: Option<String>,
    modifier: Modifier = Modifier,
    onItemClick: (ItemUiModel) -> Unit,
    navigation: HomeScreenNavigation,
    onDeleteItemClicked: (ItemUiModel) -> Unit,
    isRefreshing: IsRefreshingState,
    onRefresh: () -> Unit
) {
    ItemsList(
        modifier = modifier,
        items = items,
        highlight = highlight,
        emptyListMessage = R.string.message_no_saved_credentials,
        onItemClick = onItemClick,
        onRefresh = onRefresh,
        isRefreshing = isRefreshing,
        itemActions = listOf(
            ItemAction(
                onSelect = { goToEdit(navigation, it) },
                title = R.string.action_edit_placeholder,
                icon = me.proton.core.presentation.R.drawable.ic_proton_eraser,
                textColor = ProtonTheme.colors.textNorm
            ),
            ItemAction(
                onSelect = { onDeleteItemClicked(it) },
                title = R.string.action_move_to_trash,
                icon = me.proton.core.presentation.R.drawable.ic_proton_trash,
                textColor = ProtonTheme.colors.notificationError
            )
        )
    )
}

internal fun goToEdit(
    navigation: HomeScreenNavigation,
    item: ItemUiModel
) {
    when (item.itemType) {
        is ItemType.Login -> navigation.toEditLogin(item.shareId, item.id)
        is ItemType.Note -> navigation.toEditNote(item.shareId, item.id)
        is ItemType.Alias -> navigation.toEditAlias(item.shareId, item.id)
        ItemType.Password -> {}
    }
}
