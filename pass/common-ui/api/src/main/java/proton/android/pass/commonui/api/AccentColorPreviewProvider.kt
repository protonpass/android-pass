package proton.android.pass.commonui.api

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class AccentColorPreviewProvider : PreviewParameterProvider<Color> {
    override val values: Sequence<Color>
        get() = sequenceOf(
            PassColors.PurpleAccent,
            PassColors.GreenAccent,
            PassColors.YellowAccent
        )
}
