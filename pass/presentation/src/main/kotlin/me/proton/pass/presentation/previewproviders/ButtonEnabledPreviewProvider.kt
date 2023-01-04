package me.proton.pass.presentation.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.presentation.uievents.IsButtonEnabled

class ButtonEnabledPreviewProvider : PreviewParameterProvider<IsButtonEnabled> {
    override val values: Sequence<IsButtonEnabled>
        get() = sequenceOf(IsButtonEnabled.Enabled, IsButtonEnabled.Disabled)
}
