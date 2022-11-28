package me.proton.pass.presentation.components.common.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T : BottomSheetItem> BottomSheetItemList(
    modifier: Modifier = Modifier,
    items: List<T>
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
