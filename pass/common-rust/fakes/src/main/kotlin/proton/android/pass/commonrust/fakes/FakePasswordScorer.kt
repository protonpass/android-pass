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

package proton.android.pass.commonrust.fakes

import proton.android.pass.commonrust.api.PasswordEvaluation
import proton.android.pass.commonrust.api.PasswordPenalty
import proton.android.pass.commonrust.api.PasswordScore
import proton.android.pass.commonrust.api.PasswordScorer
import proton.android.pass.commonrust.api.toPasswordStrength
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakePasswordScorer @Inject constructor() : PasswordScorer {

    private val definedScores = mutableMapOf<String, PasswordScore>()
    private val definedPenalties = mutableMapOf<String, List<PasswordPenalty>>()

    fun defineScore(input: String, score: PasswordScore) {
        definedScores[input] = score
    }

    fun definePenalties(input: String, penalties: List<PasswordPenalty>) {
        definedPenalties[input] = penalties
    }

    override fun check(input: String): PasswordScore = definedScores.getOrElse(input) {
        PasswordScore.STRONG
    }

    override fun evaluate(input: String): PasswordEvaluation = if (input.isEmpty()) {
        PasswordEvaluation.Empty
    } else {
        PasswordEvaluation(
            strength = check(input).toPasswordStrength(),
            penalties = definedPenalties[input].orEmpty()
        )
    }
}
