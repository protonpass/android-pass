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

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random

class WordPasswordGeneratorTest {

    @Test
    fun `count = 0 returns empty string`() {
        test(
            spec = PasswordGenerator.WordPasswordSpec(count = 0),
            expected = ""
        )
    }

    @Test
    fun `count = 1 with hyphen separator`() {
        test(
            spec = PasswordGenerator.WordPasswordSpec(
                count = 1,
                separator = PasswordGenerator.WordSeparator.Hyphen
            ),
            expected = "aloft"
        )
    }

    @Test
    fun `count = 2 with space separator and capitalise`() {
        test(
            spec = PasswordGenerator.WordPasswordSpec(
                count = 2,
                separator = PasswordGenerator.WordSeparator.Space,
                capitalise = true
            ),
            expected = "Aloft Clamp"
        )
    }

    @Test
    fun `count = 3 with period separator and include numbers`() {
        test(
            spec = PasswordGenerator.WordPasswordSpec(
                count = 3,
                separator = PasswordGenerator.WordSeparator.Period,
                includeNumbers = true
            ),
            expected = "aloft0.outpour3.tremble8"
        )
    }

    @Test
    fun `count = 4 with comma separator and capitalise and include numbers`() {
        test(
            spec = PasswordGenerator.WordPasswordSpec(
                count = 4,
                separator = PasswordGenerator.WordSeparator.Comma,
                capitalise = true,
                includeNumbers = true
            ),
            expected = "Aloft0,Outpour3,Tremble8,Sinister6"
        )
    }

    @Test
    fun `count = 5 with underscore separator and include numbers and symbols`() {
        test(
            spec = PasswordGenerator.WordPasswordSpec(
                count = 5,
                separator = PasswordGenerator.WordSeparator.Underscore,
                includeNumbers = true
            ),
            expected = "aloft0_outpour3_tremble8_sinister6_opposite1"
        )
    }

    @Test
    fun `count = 6 with numbers separator and capitalise and include numbers and symbols`() {
        test(
            spec = PasswordGenerator.WordPasswordSpec(
                count = 6,
                separator = PasswordGenerator.WordSeparator.Numbers,
                capitalise = true,
                includeNumbers = true
            ),
            expected = "Aloft01Outpour38Tremble85Sinister63Opposite15Cotton0"
        )
    }

    @Test
    fun `count = 7 with numbers and symbols separator and capitalise and include numbers and symbols`() {
        test(
            spec = PasswordGenerator.WordPasswordSpec(
                count = 7,
                separator = PasswordGenerator.WordSeparator.NumbersAndSymbols,
                capitalise = true,
                includeNumbers = true
            ),
            expected = "Aloft0%Outpour3#Tremble8%Sinister6!Opposite1!Cotton0@Refueling8"
        )
    }

    private fun test(
        spec: PasswordGenerator.WordPasswordSpec,
        expected: String
    ) {
        val res = PasswordGenerator.generateWordPassword(
            spec = spec,
            random = Random(1234)
        )
        assertThat(res).isEqualTo(expected)
    }

}
