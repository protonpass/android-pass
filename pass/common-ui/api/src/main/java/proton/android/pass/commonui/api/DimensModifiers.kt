package proton.android.pass.commonui.api

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp

@Stable
fun Modifier.bottomSheet(horizontalPadding: Dp? = null): Modifier = composed {
    background(PassTheme.colors.bottomSheetBackground)
        .padding(
            vertical = PassTheme.dimens.bottomsheetVerticalPadding,
        )
        .applyIf(
            condition = horizontalPadding != null,
            ifTrue = {
                horizontalPadding?.let {
                    padding(horizontal = it)
                } ?: this
            }
        )
}
