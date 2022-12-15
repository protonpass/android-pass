package me.proton.pass.presentation.create.alias

object AliasUtils {

    private const val SPACE_REPLACEMENT_CHAR = '-'

    fun formatAlias(value: String): String {
        val noSpaces = value.replace(" ", SPACE_REPLACEMENT_CHAR.toString())
        return noSpaces
            .filter { it == SPACE_REPLACEMENT_CHAR || it.isLetterOrDigit() }
            .lowercase()
    }
}
