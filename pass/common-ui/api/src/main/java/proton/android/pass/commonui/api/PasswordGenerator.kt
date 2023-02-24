package proton.android.pass.commonui.api

import kotlin.random.Random

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
        object LettersNumbersSymbols : Option(
            setOf(
                CharacterSet.LETTERS,
                CharacterSet.NUMBERS,
                CharacterSet.SYMBOLS
            )
        )

        val dictionary = characterSets.joinToString("") { it.value }
    }

    fun generatePassword(
        length: Int = DEFAULT_LENGTH,
        option: Option = Option.LettersNumbersSymbols,
        random: Random = Random
    ): String {
        if (length == 0) return ""

        return when (option) {
            Option.Letters -> (0 until length)
                .map { option.dictionary.random(random) }
                .joinToString("")

            Option.LettersAndNumbers -> {
                val initial = (0 until length - 1)
                    .map { option.dictionary.random(random) }
                    .joinToString("")

                // Check if it contains at least a number
                val containsNumber = CharacterSet.NUMBERS.value.any { initial.contains(it) }
                if (!containsNumber) {
                    initial + CharacterSet.NUMBERS.value.random(random)
                } else {
                    initial + option.dictionary.random(random)
                }
            }
            Option.LettersNumbersSymbols -> {
                val initial = (0 until length - 2)
                    .map { option.dictionary.random(random) }
                    .joinToString("")

                // Check if it contains at least a number
                val containsNumber = CharacterSet.NUMBERS.value.any { initial.contains(it) }
                val withNumber = if (!containsNumber) {
                    initial + CharacterSet.NUMBERS.value.random(random)
                } else {
                    initial + option.dictionary.random(random)
                }

                // This is an edge case that should not happen, but we need to handle it manually
                if (length == 1) return withNumber

                // Check if it contains at least a symbol
                val containsSymbol = CharacterSet.SYMBOLS.value.any { withNumber.contains(it) }
                if (!containsSymbol) {
                    withNumber + CharacterSet.SYMBOLS.value.random(random)
                } else {
                    withNumber + option.dictionary.random(random)
                }
            }
        }
    }
}
