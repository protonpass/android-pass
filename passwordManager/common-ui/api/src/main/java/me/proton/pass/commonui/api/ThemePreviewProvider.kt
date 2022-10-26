package me.proton.pass.commonui.api

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class ThemePreviewProvider : PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean> = sequenceOf(true, false)
}

