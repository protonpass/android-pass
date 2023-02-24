package proton.android.pass.composecomponents.impl.generatepassword

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import me.proton.core.util.kotlin.times

class GeneratePasswordStatePreviewProvider : PreviewParameterProvider<GeneratePasswordUiState> {
    override val values: Sequence<GeneratePasswordUiState>
        get() = sequenceOf(
            GeneratePasswordUiState(
                password = "a1b!c_d3e#fg",
                length = 12,
                hasSpecialCharacters = true
            ),
            GeneratePasswordUiState(
                password = "a1!2",
                length = 4,
                hasSpecialCharacters = false
            ),
            GeneratePasswordUiState(
                password = "a".times(64),
                length = 64,
                hasSpecialCharacters = false
            )
        )
}
