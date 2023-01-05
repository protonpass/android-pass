package me.proton.android.pass.featurecreateitem.impl.password

data class CreatePasswordUiState(
    val password: String,
    val length: Int,
    val hasSpecialCharacters: Boolean
)
