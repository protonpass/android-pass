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

import junit.framework.TestCase.assertEquals
import org.junit.Test

class StringUtilsTest {

    @Test
    fun `maskCreditCardNumber should return empty string for empty input`() {
        assertEquals("", StringUtils.maskCreditCardNumber(""))
    }

    @Test
    fun `maskCreditCardNumber should mask the middle digits of a valid card number`() {
        val cardNumber = "1234 5678 9012 3456"
        val expectedMaskedNumber = "1234 •••• •••• 3456"
        assertEquals(expectedMaskedNumber, StringUtils.maskCreditCardNumber(cardNumber))
    }

    @Test
    fun `maskCreditCardNumber should handle input with fewer than four digits`() {
        val cardNumber = "123"
        assertEquals("123", StringUtils.maskCreditCardNumber(cardNumber))
    }

    @Test
    fun `maskCreditCardNumber should handle input with exactly four digits`() {
        val cardNumber = "1234"
        assertEquals("1234", StringUtils.maskCreditCardNumber(cardNumber))
    }

    @Test
    fun `maskCreditCardNumber should handle input with more than 16 digits`() {
        val cardNumber = "12345678901234567890"
        val expectedMaskedNumber = "1234 •••• •••• •••• 7890"
        assertEquals(expectedMaskedNumber, StringUtils.maskCreditCardNumber(cardNumber))
    }
}
