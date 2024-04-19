/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.usecases.breach

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.BreachRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.breach.ResendVerificationCode
import proton.android.pass.domain.breach.BreachEmailId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResendVerificationCodeImpl @Inject constructor(
    private val breachRepository: BreachRepository,
    private val observeCurrentUser: ObserveCurrentUser
) : ResendVerificationCode {

    override suspend fun invoke(userId: UserId?, id: BreachEmailId.Custom) = if (userId != null) {
        breachRepository.resendVerificationCode(userId, id)
    } else {
        val user = observeCurrentUser().filterNotNull().first()
        breachRepository.resendVerificationCode(user.userId, id)
    }
}
