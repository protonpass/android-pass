package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.commonui.api.PassTheme

@Composable
fun BottomSheetItemList(
    modifier: Modifier = Modifier,
    items: ImmutableList<BottomSheetItem>
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        items.forEach { item ->
            if (item.isDivider) {
                Divider(color = PassTheme.colors.interactionNormMinor1)
            } else {
                BottomSheetItemRow(
                    title = item.title,
                    subtitle = item.subtitle,
                    leftIcon = item.leftIcon,
                    endIcon = item.endIcon,
                    onClick = item.onClick?.let { { it.invoke() } }
                )
            }
        }
    }
}
