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

package proton.android.pass.commonrust.api

import proton.android.pass.common.api.PasswordStrength

interface PasswordScorer {
    fun check(input: String): PasswordScore
    fun evaluate(input: String): PasswordEvaluation
}

enum class PasswordScore {
    VULNERABLE,
    STRONG,
    WEAK
}

fun PasswordScore.toPasswordStrength(): PasswordStrength = when (this) {
    PasswordScore.VULNERABLE -> PasswordStrength.Vulnerable
    PasswordScore.WEAK -> PasswordStrength.Weak
    PasswordScore.STRONG -> PasswordStrength.Strong
}

enum class PasswordPenalty {
    NoLowercase,
    NoUppercase,
    NoNumbers,
    NoSymbols,
    Short,
    Consecutive,
    Progressive,
    ContainsCommonPassword
}

data class PasswordEvaluation(
    val strength: PasswordStrength,
    val penalties: List<PasswordPenalty>
) {
    companion object {
        // Sentinel for empty input. Penalties are restricted to the UI-surfaced checks
        // (Short / NoLowercase / NoUppercase / Consecutive / ContainsCommonPassword);
        // the other 3 (NoNumbers / NoSymbols / Progressive) are dropped
        // because they are not rendered. Both prod and fake scorers must reference this
        // single source of truth so tests reflect production behaviour.
        val Empty: PasswordEvaluation = PasswordEvaluation(
            strength = PasswordStrength.None,
            penalties = listOf(
                PasswordPenalty.Short,
                PasswordPenalty.NoLowercase,
                PasswordPenalty.NoUppercase,
                PasswordPenalty.Consecutive,
                PasswordPenalty.ContainsCommonPassword
            )
        )
    }
}
