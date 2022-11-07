package me.proton.pass.presentation.components.common.item

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.google.accompanist.swiperefresh.SwipeRefreshState
import me.proton.pass.presentation.components.common.PassSwipeRefresh
import me.proton.pass.presentation.components.model.ItemUiModel
import me.proton.pass.presentation.uievents.IsRefreshingState

@Composable
fun ItemsList(
    items: List<ItemUiModel>,
    @StringRes emptyListMessage: Int,
    modifier: Modifier = Modifier,
    itemActions: List<ItemAction> = emptyList(),
    onRefresh: () -> Unit,
    isRefreshing: IsRefreshingState,
    onItemClick: (ItemUiModel) -> Unit = {}
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
