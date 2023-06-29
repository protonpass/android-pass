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
import java.security.SecureRandom

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
            expected = "activate"
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
            expected = "Activate Sincere"
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
            expected = "activate1.gulf5.impulse6"
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
            expected = "Activate1,Gulf5,Impulse6,Prude0"
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
            expected = "activate1_gulf5_impulse6_prude0_monday2"
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
            expected = "Activate16Gulf53Impulse62Prude05Monday22Pardon7"
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
            expected = "Activate1@Gulf5%Impulse6@Prude06Monday29Pardon77Pancreas3"
        )
    }

    private fun test(
        spec: PasswordGenerator.WordPasswordSpec,
        expected: String
    ) {
        val res = PasswordGenerator.generateWordPassword(
            spec = spec,
            random = SecureRandom.getInstance("SHA1PRNG").apply {
                setSeed(1234L)
            }
        )
        assertThat(res).isEqualTo(expected)
    }

}
