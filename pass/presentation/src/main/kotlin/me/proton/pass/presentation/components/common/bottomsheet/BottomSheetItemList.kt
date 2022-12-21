package me.proton.pass.presentation.components.common.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T : BottomSheetItem> BottomSheetItemList(
    modifier: Modifier = Modifier,
    items: ImmutableList<T>
) {
    Column(modifier) {
        items.forEach { item ->
            BottomSheetItemRow(
                title = item.title,
                subtitle = item.subtitle,
                icon = item.icon,
                onClick = { item.onClick() }
            )
        }
    }
}
