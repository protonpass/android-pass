package me.proton.pass.presentation

object PasswordGenerator {
    const val DEFAULT_LENGTH = 16

    enum class CharacterSet(val value: String) {
        LETTERS("abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ"),
        NUMBERS("0123456789"),
        SYMBOLS("!#\$%&()*+.:;<=>?@[]^")
    }

    sealed class Option(characterSets: Set<CharacterSet>) {
        object Letters : Option(setOf(CharacterSet.LETTERS))
        object LettersAndNumbers : Option(setOf(CharacterSet.LETTERS, CharacterSet.NUMBERS))
        object LettersNumbersSymbols : Option(setOf(CharacterSet.LETTERS, CharacterSet.NUMBERS, CharacterSet.SYMBOLS))

        val dictionary = characterSets.joinToString("") { it.value }
    }

    fun generatePassword(
        length: Int = DEFAULT_LENGTH,
        option: Option = Option.LettersNumbersSymbols
    ) = (0 until length)
        .map { option.dictionary.random() }
        .joinToString("")
}
