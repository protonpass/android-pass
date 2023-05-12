package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.runtime.Immutable
import proton.android.pass.password.api.PasswordGenerator

@Immutable
enum class GeneratePasswordMode {
    CopyAndClose,
    CancelConfirm;
}

@Immutable
data class GeneratePasswordUiState(
    val password: String,
    val mode: GeneratePasswordMode,
    val content: GeneratePasswordContent
)

sealed interface GeneratePasswordContent {
    @Immutable
    data class RandomPassword(
        val length: Int,
        val hasSpecialCharacters: Boolean,
        val hasCapitalLetters: Boolean,
        val includeNumbers: Boolean
    ) : GeneratePasswordContent

    @Immutable
    data class WordsPassword(
        val count: Int,
        val wordSeparator: PasswordGenerator.WordSeparator,
        val capitalise: Boolean,
        val includeNumbers: Boolean
    ) : GeneratePasswordContent

}
