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

import proton.android.pass.commonrust.PasswordScore
import proton.android.pass.commonrust.PasswordScorer
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrength
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import javax.inject.Inject

class RustPasswordStrengthCalculator @Inject constructor() : PasswordStrengthCalculator {

    private val passwordScorer: PasswordScorer by lazy { PasswordScorer() }
    
    override fun calculateStrength(password: String): PasswordStrength = if (password.isEmpty()) {
        PasswordStrength.None
    } else {
        when (passwordScorer.checkScore(password)) {
            PasswordScore.VERY_DANGEROUS,
            PasswordScore.DANGEROUS -> PasswordStrength.Vulnerable

            PasswordScore.VERY_WEAK,
            PasswordScore.WEAK -> PasswordStrength.Weak

            PasswordScore.GOOD,
            PasswordScore.STRONG,
            PasswordScore.VERY_STRONG,
            PasswordScore.INVULNERABLE -> PasswordStrength.Strong
        }
    }

}
