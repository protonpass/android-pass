package me.proton.pass.presentation.components.previewproviders

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.pass.presentation.detail.login.PasswordState

class PasswordStatePreviewProvider : PreviewParameterProvider<PasswordState> {
    override val values: Sequence<PasswordState>
        get() = sequenceOf(
            PasswordState.Concealed("encrypted"),
            PasswordState.Revealed("encrypted", "clearText")
        )
}
