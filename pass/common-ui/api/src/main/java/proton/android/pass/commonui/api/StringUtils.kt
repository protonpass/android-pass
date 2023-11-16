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

package proton.android.pass.commonui.api

object StringUtils {
    private const val CREDIT_CARD_CHUNK_SIZE = 4

    fun maskCreditCardNumber(cardNumber: String): String {
        val cleanedNumber = cardNumber.replace("\\D".toRegex(), "")
        if (cleanedNumber.isEmpty()) {
            return ""
        }
        return buildString {
            for (i in cleanedNumber.indices) {
                if (i < CREDIT_CARD_CHUNK_SIZE || i >= cleanedNumber.length - CREDIT_CARD_CHUNK_SIZE) {
                    append(cleanedNumber[i])
                } else {
                    append('•')
                }
                if ((i + 1) % CREDIT_CARD_CHUNK_SIZE == 0 && i < cleanedNumber.length - 1) {
                    append(" ")
                }
            }
        }
    }
}
