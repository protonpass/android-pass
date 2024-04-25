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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import proton.android.pass.data.api.repositories.BreachRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.breach.ObserveBreachEmail
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachEmailId
import javax.inject.Inject

class ObserveBreachEmailImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val breachRepository: BreachRepository
) : ObserveBreachEmail {

    override fun invoke(breachEmailId: BreachEmailId): Flow<BreachCustomEmail> =
        observeCurrentUser()
            .flatMapLatest { user ->
                when (breachEmailId) {
                    is BreachEmailId.Alias -> TODO()
                    is BreachEmailId.Custom -> breachRepository.observeCustomEmail(
                        userId = user.userId,
                        customEmailId = breachEmailId
                    )

                    is BreachEmailId.Proton -> TODO()
                }
            }

}
