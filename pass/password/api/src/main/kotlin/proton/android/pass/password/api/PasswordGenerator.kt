/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.password.api

import java.util.Locale
import kotlin.random.Random

object PasswordGenerator {
    private const val LOWERCASE_LETTERS = "abcdefghjkmnpqrstuvwxyz"
    private const val CAPITAL_LETTERS = "ABCDEFGHJKMNPQRSTUVWXYZ"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*"

    enum class WordSeparator {
        Hyphen,
        Space,
        Period,
        Comma,
        Underscore,
        Numbers,
        NumbersAndSymbols
    }

    data class WordPasswordSpec(
        val count: Int = 4,
        val separator: WordSeparator = WordSeparator.Hyphen,
        val capitalise: Boolean = false,
        val includeNumbers: Boolean = false
    )

    data class RandomPasswordSpec(
        val length: Int = 12,
        val hasCapitalLetters: Boolean = false,
        val hasNumbers: Boolean = true,
        val hasSymbols: Boolean = true
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

    @Suppress("MagicNumber")
    fun generatePassword(
        spec: RandomPasswordSpec,
        random: Random = Random
    ): String {
        if (spec.length == 0) return ""
        val dictionary = getRandomDictionary(spec)

        return when {
            // We don't allow to generate passwords with less than 3, so we will just do a
            // best-effort policy here
            spec.length <= 3 -> buildString {
                repeat(spec.length) {
                    append(dictionary.random(random))
                }
            }
            else -> {
                var generated = ""
                (0 until spec.length - 3).forEach { _ ->
                    generated += dictionary.random(random)
                }

                generated += if (spec.hasCapitalLetters && !generated.containsCapitalLetters()) {
                    CAPITAL_LETTERS.random(random)
                } else {
                    dictionary.random(random)
                }

                generated += if (spec.hasNumbers && !generated.containsNumbers()) {
                    NUMBERS.random(random)
                } else {
                    dictionary.random(random)
                }

                generated += if (spec.hasSymbols && !generated.containsSymbols()) {
                    SYMBOLS.random(random)
                } else {
                    dictionary.random(random)
                }

                generated
            }
        }
    }

    private fun getRandomDictionary(spec: RandomPasswordSpec): String = buildString {
        append(LOWERCASE_LETTERS)
        if (spec.hasNumbers) {
            append(NUMBERS)
        }
        if (spec.hasCapitalLetters) {
            append(CAPITAL_LETTERS)
        }
        if (spec.hasSymbols) {
            append(SYMBOLS)
        }
    }

    private fun joinWithSeparator(words: List<String>, separator: WordSeparator, random: Random) =
        buildString {
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
                val dictionary = "${NUMBERS}$SYMBOLS"
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

    fun String.containsCapitalLetters(): Boolean = containsList(CAPITAL_LETTERS)
    fun String.containsNumbers(): Boolean = containsList(NUMBERS)
    fun String.containsSymbols(): Boolean = containsList(SYMBOLS)

    private fun String.containsList(list: String): Boolean = any { list.contains(it) }
}
