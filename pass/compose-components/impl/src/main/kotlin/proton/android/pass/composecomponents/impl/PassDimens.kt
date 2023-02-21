package proton.android.pass.composecomponents.impl

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object PassDimens {
    val DefaultCornerRadius = 16.dp

    object BottomSheet {
        val HorizontalPadding = 16.dp
        val VerticalPadding = 24.dp
    }

    fun Modifier.bottomSheetPadding(): Modifier = padding(
        horizontal = BottomSheet.HorizontalPadding,
        vertical = BottomSheet.VerticalPadding
    )
}
