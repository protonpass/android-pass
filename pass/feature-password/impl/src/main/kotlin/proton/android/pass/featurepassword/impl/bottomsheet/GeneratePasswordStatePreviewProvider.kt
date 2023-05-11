package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class GeneratePasswordStatePreviewProvider : PreviewParameterProvider<GeneratePasswordUiState> {
    override val values: Sequence<GeneratePasswordUiState>
        get() = sequenceOf(
            GeneratePasswordUiState(
                password = "a1b!c_d3e#fg",
                length = 12,
                hasSpecialCharacters = true,
                mode = GeneratePasswordMode.CopyAndClose
            ),
            GeneratePasswordUiState(
                password = "a1!2",
                length = 4,
                hasSpecialCharacters = false,
                mode = GeneratePasswordMode.CopyAndClose
            ),
            GeneratePasswordUiState(
                password = buildString { repeat(64) { append("a") } },
                length = 64,
                hasSpecialCharacters = false,
                mode = GeneratePasswordMode.CancelConfirm
            )
        )
}
