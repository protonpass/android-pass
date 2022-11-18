package me.proton.android.pass.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.android.pass.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.domain.ItemType
import me.proton.pass.presentation.components.common.item.ItemAction
import me.proton.pass.presentation.components.common.item.ItemsList
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.IsRefreshingState

@Composable
internal fun Home(
    items: List<ItemUiModel>,
    highlight: String?,
    modifier: Modifier = Modifier,
    onItemClick: (ItemUiModel) -> Unit,
    navigation: HomeScreenNavigation,
    onDeleteItemClicked: (ItemUiModel) -> Unit,
    isRefreshing: IsRefreshingState,
    isLoading: IsLoadingState,
    onRefresh: () -> Unit
) {
    ItemsList(
        modifier = modifier,
        items = items,
        highlight = highlight,
        emptyListMessage = R.string.empty_list_home_subtitle,
        onItemClick = onItemClick,
        onRefresh = onRefresh,
        isRefreshing = isRefreshing,
        isLoading = isLoading,
        itemActions = listOf(
            ItemAction(
                onSelect = { goToEdit(navigation, it) },
                title = me.proton.pass.presentation.R.string.action_edit_placeholder,
                icon = me.proton.core.presentation.R.drawable.ic_proton_eraser,
                textColor = ProtonTheme.colors.textNorm
            ),
            ItemAction(
                onSelect = { onDeleteItemClicked(it) },
                title = me.proton.pass.presentation.R.string.action_move_to_trash,
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
