package proton.android.pass.commonui.api

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

@Stable
fun Modifier.bottomSheetPadding(): Modifier = composed {
    padding(
        horizontal = PassTheme.dimens.bottomsheetHorizontalPadding,
        vertical = PassTheme.dimens.bottomsheetVerticalPadding
    )
}
