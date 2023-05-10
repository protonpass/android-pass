package proton.android.pass.password.api

import java.util.Locale
import kotlin.random.Random

object PasswordGenerator {
    const val DEFAULT_LENGTH = 16

    enum class CharacterSet(val value: String) {
        LETTERS("abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ"),
        NUMBERS("0123456789"),
        SYMBOLS("!@#$%^&*")
    }

    enum class WordSeparator {
        Hyphen,
        Space,
        Period,
        Comma,
        Underscore,
        Numbers,
        NumbersAndSymbols
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

    data class WordPasswordSpec(
        val count: Int = 4,
        val separator: WordSeparator = WordSeparator.Hyphen,
        val capitalise: Boolean = false,
        val includeNumbers: Boolean = false
    )

    fun generateWordPassword(
        spec: WordPasswordSpec,
        random: Random = Random
    ): String {
        if (spec.count == 0) return ""
        val words = (0 until spec.count).map {
            val randomWord = WORDS.random(random)
            val capitalisedWord = if (spec.capitalise) {
                capitalise(randomWord)
            } else {
                randomWord
            }

            if (spec.includeNumbers) {
                val number = random.nextInt(from = 0, until = 9)
                "${capitalisedWord}$number"
            } else {
                capitalisedWord
            }
        }

        return joinWithSeparator(words, spec.separator, random)
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

    private fun joinWithSeparator(words: List<String>, separator: WordSeparator, random: Random) = buildString {
        if (words.isEmpty()) return@buildString

        append(words.first())

        words.drop(1).forEach { word ->
            append(getSeparator(separator, random))
            append(word)
        }
    }

    private fun getSeparator(separator: WordSeparator, random: Random): Char =
        when (separator) {
            WordSeparator.Hyphen -> '-'
            WordSeparator.Space -> ' '
            WordSeparator.Period -> '.'
            WordSeparator.Comma -> ','
            WordSeparator.Underscore -> '_'
            WordSeparator.Numbers -> random.nextInt(from = 0, until = 9).digitToChar()
            WordSeparator.NumbersAndSymbols -> {
                val dictionary = "${CharacterSet.NUMBERS.value}${CharacterSet.SYMBOLS.value}"
                dictionary.random(random)
            }
        }


    private fun capitalise(word: String): String =
        word.replaceFirstChar { firstChar ->
            if (firstChar.isLowerCase()) {
                firstChar.titlecase(Locale.getDefault())
            } else {
                firstChar.toString()
            }
        }
}
