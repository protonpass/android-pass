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

package proton.android.pass.commonrust.impl

import proton.android.pass.commonrust.api.PasswordEvaluation
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonrust.api.PasswordScorer
import proton.android.pass.commonrust.api.toPasswordStrength
import proton.android.pass.commonrust.impl.passwords.toPasswordPenalty
import javax.inject.Inject
import javax.inject.Singleton
import proton.android.pass.commonrust.PasswordScore as RustPasswordScore
import proton.android.pass.commonrust.PasswordScorer as RustPasswordScorer

@Singleton
class PasswordScorerImpl @Inject constructor() : PasswordScorer {

    private val rustScorer: RustPasswordScorer by lazy { RustPasswordScorer() }

    override fun check(input: String): PasswordScore = rustScorer.checkScore(input).toPasswordScore()

    override fun evaluate(input: String): PasswordEvaluation {
        if (input.isEmpty()) return PasswordEvaluation.Empty
        val result = rustScorer.scorePassword(input)
        return PasswordEvaluation(
            strength = result.passwordScore.toPasswordScore().toPasswordStrength(),
            penalties = result.penalties.map { it.toPasswordPenalty() }
        )
    }
}

internal fun RustPasswordScore.toPasswordScore(): PasswordScore = when (this) {
    RustPasswordScore.WEAK -> PasswordScore.WEAK
    RustPasswordScore.VULNERABLE -> PasswordScore.VULNERABLE
    RustPasswordScore.STRONG -> PasswordScore.STRONG
}
