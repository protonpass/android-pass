/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.commonrust.api.passwords

import proton.android.pass.commonrust.api.WordSeparator

sealed interface PasswordConfig {

    data class Random(
        val includeUppercase: Boolean,
        val includeNumbers: Boolean,
        val includeSymbols: Boolean,
        val canToggleUppercase: Boolean = true,
        val canToggleNumbers: Boolean = true,
        val canToggleSymbols: Boolean = true,
        private val passwordLength: Int,
        private val passwordMinLength: Int? = null,
        private val passwordMaxLength: Int? = null
    ) : PasswordConfig {

        val length: Int = passwordLength.coerceIn(range = PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH)

        val minLength: Int = passwordMinLength
            ?.coerceIn(range = PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH)
            ?: PASSWORD_MIN_LENGTH

        val maxLength: Int = passwordMaxLength
            ?.coerceIn(range = PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH)
            ?: PASSWORD_MAX_LENGTH

    }

    data class Memorable(
        val capitalizeWords: Boolean,
        val includeNumbers: Boolean,
        val canToggleCapitalise: Boolean = true,
        val canToggleNumbers: Boolean = true,
        private val passwordWordsCount: Int,
        private val passwordWordsSeparator: WordSeparator,
        private val passwordMinWords: Int? = null,
        private val passwordMaxWords: Int? = null
    ) : PasswordConfig {

        val wordsCount: Int = passwordWordsCount
            .coerceIn(range = PASSWORD_MIN_WORDS..PASSWORD_MAX_WORDS)

        val minWordsCount: Int = passwordMinWords
            ?.coerceIn(range = PASSWORD_MIN_WORDS..PASSWORD_MAX_WORDS)
            ?: PASSWORD_MIN_WORDS

        val maxWordsCount: Int = passwordMaxWords
            ?.coerceIn(range = PASSWORD_MIN_WORDS..PASSWORD_MAX_WORDS)
            ?: PASSWORD_MAX_WORDS

        val wordSeparator: WordSeparator = passwordWordsSeparator

    }

    private companion object {

        private const val PASSWORD_MIN_LENGTH = 4

        private const val PASSWORD_MAX_LENGTH = 64

        private const val PASSWORD_MIN_WORDS = 1

        private const val PASSWORD_MAX_WORDS = 10

    }

}
