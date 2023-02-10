package proton.android.pass.featuresettings.impl

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled

class ButtonEnabledPreviewProvider : PreviewParameterProvider<IsButtonEnabled> {
    override val values: Sequence<IsButtonEnabled>
        get() = sequenceOf(IsButtonEnabled.Enabled, IsButtonEnabled.Disabled)
}
