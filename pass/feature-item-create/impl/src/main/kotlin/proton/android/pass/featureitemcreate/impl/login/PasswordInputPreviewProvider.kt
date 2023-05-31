package proton.android.pass.featureitemcreate.impl.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.pass.domain.HiddenState

class PasswordInputPreviewProvider : PreviewParameterProvider<PasswordInputPreviewParams> {
    override val values: Sequence<PasswordInputPreviewParams>
        get() = sequenceOf(
            PasswordInputPreviewParams(
                HiddenState.Revealed("", "someValue"),
                false
            ),
            PasswordInputPreviewParams(
                HiddenState.Revealed("", "someValue"),
                true
            ),
            PasswordInputPreviewParams(
                HiddenState.Concealed(""),
                false,
            ),
            PasswordInputPreviewParams(
                HiddenState.Concealed(""),
                true
            )
        )
}

data class PasswordInputPreviewParams(
    val hiddenState: HiddenState,
    val isEditAllowed: Boolean
)
