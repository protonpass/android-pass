package me.proton.pass.presentation.home.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <T> SortingTypeBottomSheetItemList(
    modifier: Modifier = Modifier,
    items: List<T>,
    displayer: (T) -> String,
    isChecked: (T) -> Boolean,
    onSelect: (T) -> Unit
) {
    Column(modifier) {
        items.forEach { item ->
            SortingBottomSheetItem(
                text = displayer(item),
                isChecked = isChecked(item)
            ) {
                onSelect(item)
            }
        }
    }
}
