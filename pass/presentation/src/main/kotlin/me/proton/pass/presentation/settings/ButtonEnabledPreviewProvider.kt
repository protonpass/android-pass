package me.proton.pass.presentation.settings

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled

class ButtonEnabledPreviewProvider : PreviewParameterProvider<IsButtonEnabled> {
    override val values: Sequence<IsButtonEnabled>
        get() = sequenceOf(IsButtonEnabled.Enabled, IsButtonEnabled.Disabled)
}
