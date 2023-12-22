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

package proton.android.pass.commonrust.impl.passwords.strengths

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonrust.PasswordScore

@RunWith(Parameterized::class)
internal class RustPasswordStrengthMapperTest(
    private val passwordScore: PasswordScore,
    private val expectedPasswordStrength: PasswordStrength,
) {

    @Test
    internal fun `WHEN mapping password scores THEN return expected password strengths`() {
        val passwordStrength = passwordScore.toPasswordStrength()

        assertThat(passwordStrength).isEqualTo(expectedPasswordStrength)
    }

    private companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun scoreToStrengthMapping() = listOf(
            arrayOf(PasswordScore.INVULNERABLE, PasswordStrength.Strong),
            arrayOf(PasswordScore.VERY_STRONG, PasswordStrength.Strong),
            arrayOf(PasswordScore.STRONG, PasswordStrength.Strong),
            arrayOf(PasswordScore.GOOD, PasswordStrength.Strong),
            arrayOf(PasswordScore.WEAK, PasswordStrength.Weak),
            arrayOf(PasswordScore.VERY_WEAK, PasswordStrength.Weak),
            arrayOf(PasswordScore.DANGEROUS, PasswordStrength.Vulnerable),
            arrayOf(PasswordScore.VERY_DANGEROUS, PasswordStrength.Vulnerable),
        )

    }

}
