/*
 * Copyright (c) 2023-2026 Proton AG
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
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonrust.PasswordScore
import proton.android.pass.commonrust.api.toPasswordStrength
import proton.android.pass.commonrust.impl.toPasswordScore

@RunWith(TestParameterInjector::class)
internal class RustPasswordStrengthMapperTest {

    @Test
    internal fun `WHEN mapping password scores THEN return expected password strengths`(
        @TestParameter case: ScoreCase
    ) {
        assertThat(case.score.toPasswordScore().toPasswordStrength()).isEqualTo(case.expected)
    }

    internal enum class ScoreCase(
        val score: PasswordScore,
        val expected: PasswordStrength
    ) {
        Vulnerable(PasswordScore.VULNERABLE, PasswordStrength.Vulnerable),
        Strong(PasswordScore.STRONG, PasswordStrength.Strong),
        Weak(PasswordScore.WEAK, PasswordStrength.Weak)
    }
}
