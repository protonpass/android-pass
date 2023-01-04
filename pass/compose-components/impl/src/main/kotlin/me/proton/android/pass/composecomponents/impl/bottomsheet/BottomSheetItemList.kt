package me.proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList

@Composable
fun BottomSheetItemList(
    modifier: Modifier = Modifier,
    items: ImmutableList<BottomSheetItem>
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
