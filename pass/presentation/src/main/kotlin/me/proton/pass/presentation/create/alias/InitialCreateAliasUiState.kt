package me.proton.pass.presentation.create.alias

@JvmInline
value class InitialCreateAliasUiState(
    val title: String? = null
)

fun InitialCreateAliasUiState.alias(): String {
    if (title == null) return ""
    return AliasUtils.formatAlias(title)
}
