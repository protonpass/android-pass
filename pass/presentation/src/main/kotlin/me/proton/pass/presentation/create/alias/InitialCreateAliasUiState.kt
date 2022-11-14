package me.proton.pass.presentation.create.alias

data class InitialCreateAliasUiState(
    val title: String? = null
)

fun InitialCreateAliasUiState.alias(): String {
    if (title == null) return ""
    return title.filter { it.isLetterOrDigit() }.lowercase()
}
