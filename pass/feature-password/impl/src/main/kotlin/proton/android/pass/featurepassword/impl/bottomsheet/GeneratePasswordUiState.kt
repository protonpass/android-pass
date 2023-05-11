package proton.android.pass.featurepassword.impl.bottomsheet

enum class GeneratePasswordMode {
    CopyAndClose,
    CancelConfirm;
}

data class GeneratePasswordUiState(
    val password: String,
    val length: Int,
    val hasSpecialCharacters: Boolean,
    val mode: GeneratePasswordMode
)
