package proton.android.pass.composecomponents.impl.generatepassword

data class GeneratePasswordUiState(
    val password: String,
    val length: Int,
    val hasSpecialCharacters: Boolean
)
