package me.proton.android.pass.ui.trash

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.proton.android.pass.R
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.components.common.item.ItemAction
import me.proton.pass.presentation.components.common.item.ItemsList
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsRefreshingState

@Composable
internal fun TrashItemList(
    items: List<ItemUiModel>,
    modifier: Modifier = Modifier,
    onRestoreClicked: (ItemUiModel) -> Unit,
    onDeleteItemClicked: (ItemUiModel) -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: IsRefreshingState
) {
    ItemsList(
        items = items,
        emptyListMessage = R.string.message_no_trashed_credentials,
        modifier = modifier,
        onRefresh = onRefresh,
        isRefreshing = isRefreshing,
        itemActions = listOf(
            ItemAction(
                onSelect = { onRestoreClicked(it) },
                title = R.string.action_restore,
                icon = me.proton.core.presentation.R.drawable.ic_proton_eraser,
                textColor = ProtonTheme.colors.textNorm
            ),
            ItemAction(
                onSelect = { onDeleteItemClicked(it) },
                title = R.string.action_delete,
                icon = me.proton.core.presentation.R.drawable.ic_proton_trash,
                textColor = ProtonTheme.colors.notificationError
            )
        )
    )
}
