package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList

@Composable
fun <T> AliasBottomSheetItemList(
    modifier: Modifier = Modifier,
    items: ImmutableList<T>,
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
