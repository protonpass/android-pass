package me.proton.pass.presentation.create.alias

object AliasUtils {

    private const val SPACE_REPLACEMENT_CHAR = '-'
    private val ALLOWED_SPECIAL_CHARACTERS: List<Char> = listOf('_', '-', '.')

    fun formatAlias(value: String): String {
        val noSpaces = value.replace(" ", SPACE_REPLACEMENT_CHAR.toString())
        return noSpaces
            .filter { it.isLetterOrDigit() || ALLOWED_SPECIAL_CHARACTERS.contains(it) }
            .lowercase()
    }

    fun areAllAliasCharactersValid(alias: String): Boolean {
        for (char in alias) {
            // If it's not a letter or a digit, check if it's one of the allowed symbols
            if (!char.isLetterOrDigit() && !ALLOWED_SPECIAL_CHARACTERS.contains(char)) return false

            // If it's a letter, must be lowercase
            if (char.isLetter() && char.isUpperCase()) return false
        }
        return true
    }
}
