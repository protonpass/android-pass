package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

class GeneratePasswordStatePreviewProvider : PreviewParameterProvider<GeneratePasswordUiState> {
    override val values: Sequence<GeneratePasswordUiState>
        get() = sequenceOf(
            GeneratePasswordUiState(
                password = "a1b!c_d3e#fg",
                content = GeneratePasswordContent.RandomPassword(
                    length = 12,
                    hasSpecialCharacters = true,
                    hasCapitalLetters = false,
                    includeNumbers = true
                ),
                mode = GeneratePasswordMode.CopyAndClose
            ),
            GeneratePasswordUiState(
                password = "a1!2",
                content = GeneratePasswordContent.RandomPassword(
                    length = 4,
                    hasSpecialCharacters = false,
                    hasCapitalLetters = false,
                    includeNumbers = true
                ),
                mode = GeneratePasswordMode.CopyAndClose
            ),
            GeneratePasswordUiState(
                password = buildString { repeat(64) { append("a") } },
                content = GeneratePasswordContent.RandomPassword(
                    length = 64,
                    hasSpecialCharacters = false,
                    hasCapitalLetters = true,
                    includeNumbers = false
                ),
                mode = GeneratePasswordMode.CancelConfirm
            )
        )
}
