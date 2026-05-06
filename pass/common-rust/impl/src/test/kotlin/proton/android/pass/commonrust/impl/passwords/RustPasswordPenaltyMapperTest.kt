/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.commonrust.impl.passwords

import com.google.common.truth.Truth.assertThat
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.commonrust.api.PasswordPenalty
import proton.android.pass.commonrust.PasswordPenalty as RustPasswordPenalty

@RunWith(TestParameterInjector::class)
internal class RustPasswordPenaltyMapperTest {

    @Test
    internal fun `WHEN mapping rust penalty THEN returns expected api penalty`(@TestParameter case: PenaltyCase) {
        assertThat(case.rust.toPasswordPenalty()).isEqualTo(case.expected)
    }

    internal enum class PenaltyCase(
        val rust: RustPasswordPenalty,
        val expected: PasswordPenalty
    ) {
        NoLowercase(RustPasswordPenalty.NO_LOWERCASE, PasswordPenalty.NoLowercase),
        NoUppercase(RustPasswordPenalty.NO_UPPERCASE, PasswordPenalty.NoUppercase),
        NoNumbers(RustPasswordPenalty.NO_NUMBERS, PasswordPenalty.NoNumbers),
        NoSymbols(RustPasswordPenalty.NO_SYMBOLS, PasswordPenalty.NoSymbols),
        Short(RustPasswordPenalty.SHORT, PasswordPenalty.Short),
        Consecutive(RustPasswordPenalty.CONSECUTIVE, PasswordPenalty.Consecutive),
        Progressive(RustPasswordPenalty.PROGRESSIVE, PasswordPenalty.Progressive),
        ContainsCommonPassword(RustPasswordPenalty.CONTAINS_COMMON_PASSWORD, PasswordPenalty.ContainsCommonPassword)
    }
}
