package me.proton.pass.presentation.detail.login

data class LoginUiModel(
    val title: String,
    val username: String,
    val password: PasswordState,
    val websites: List<String>,
    val note: String
)
