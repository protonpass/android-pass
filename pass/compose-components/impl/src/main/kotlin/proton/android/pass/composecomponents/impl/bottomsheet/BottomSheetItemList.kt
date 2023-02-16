package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@Composable
fun BottomSheetItemList(
    modifier: Modifier = Modifier,
    items: ImmutableList<BottomSheetItem>
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp, 24.dp)
    ) {
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
