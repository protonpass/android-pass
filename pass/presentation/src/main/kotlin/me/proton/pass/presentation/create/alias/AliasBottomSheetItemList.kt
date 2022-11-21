package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> AliasBottomSheetItemList(
    modifier: Modifier = Modifier,
    items: List<T>,
    displayer: (T) -> String,
    isChecked: (T) -> Boolean,
    onSelect: (T) -> Unit
) {
    Column(modifier) {
        items.forEach { item ->
            AliasBottomSheetItem(
                text = displayer(item),
                isChecked = isChecked(item)
            ) {
                onSelect(item)
            }
        }
    }
}
