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

package proton.android.pass.commonuimodels.api.masks

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.CommonRegex
import proton.android.pass.common.api.SpecialCharacters

private const val CREDIT_CARD_CHUNK_SIZE = 4

@Stable
sealed interface TextMask {

    val masked: String
    val unmasked: String

    @Stable
    data class CardNumber(private val input: String) : TextMask {

        override val masked: String = run {
            val cleanedNumber = input.replace("\\D".toRegex(), "")
            if (cleanedNumber.isNotEmpty()) {
                buildString {
                    for (i in cleanedNumber.indices) {
                        if (i < CREDIT_CARD_CHUNK_SIZE || i >= cleanedNumber.length - CREDIT_CARD_CHUNK_SIZE) {
                            append(cleanedNumber[i])
                        } else {
                            append(SpecialCharacters.DOT_SEPARATOR)
                        }
                        if ((i + 1) % CREDIT_CARD_CHUNK_SIZE == 0 && i < cleanedNumber.length - 1) {
                            append(SpecialCharacters.SPACE)
                        }
                    }
                }
            } else {
                cleanedNumber
            }
        }

        override val unmasked: String = input
            .chunked(size = CREDIT_CARD_CHUNK_SIZE)
            .joinToString(separator = SpecialCharacters.SPACE.toString())

    }

    @Stable
    data class ExpirationDate(private val input: String) : TextMask {

        override val masked: String = if (CommonRegex.EXPIRATION_DATE_REGEX.matches(input)) {
            val month = input.takeLast(n = 2)
            val year = input.substring(startIndex = 2, endIndex = 4)
            "$month ${SpecialCharacters.SLASH} $year"
        } else {
            input
        }

        override val unmasked: String = input

    }

    @Stable
    data class TotpCode(private val input: String) : TextMask {

        override val masked: String = input.length
            .div(2)
            .let { half -> "${input.take(half)} ${SpecialCharacters.DOT_SEPARATOR} ${input.takeLast(half)}" }

        override val unmasked: String = input

    }

    @Stable
    data class TextBetweenFirstAndLastChar(
        private val input: String,
        private val replacementLength: Int = 10,
        private val replacementSymbol: Char = SpecialCharacters.DOT_SEPARATOR
    ) : TextMask {

        override val masked: String = if (input.length < 2) {
            input
        } else {
            buildString {
                append(input.first())
                repeat(replacementLength) { append(replacementSymbol) }
                append(input.last())
            }
        }

        override val unmasked: String = input

    }

}
