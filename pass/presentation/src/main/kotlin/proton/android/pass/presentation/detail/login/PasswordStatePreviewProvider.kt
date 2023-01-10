package proton.android.pass.presentation.detail.login

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class PasswordStatePreviewProvider : PreviewParameterProvider<PasswordState> {
    override val values: Sequence<PasswordState>
        get() = sequenceOf(
            PasswordState.Concealed("encrypted"),
            PasswordState.Revealed("encrypted", "clearText")
        )
}
