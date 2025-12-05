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

package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGetUserPlan @Inject constructor() : GetUserPlan {

    private val userPlans = mutableMapOf<UserId, MutableStateFlow<Result<Plan>>>()

    fun setResult(value: Result<Plan>, userId: UserId = DEFAULT_USER_ID) {
        userPlans[userId]?.tryEmit(value) ?: run { userPlans[userId] = MutableStateFlow(value) }
    }

    override fun invoke(userId: UserId?): Flow<Plan> = userPlans.getOrPut(userId ?: DEFAULT_USER_ID) {
        MutableStateFlow(Result.success(DEFAULT_PLAN))
    }.map { it.getOrThrow() }

    companion object {
        val DEFAULT_PLAN = Plan(
            planType = PlanType.Free("free", "Proton Free"),
            hideUpgrade = false,
            vaultLimit = PlanLimit.Limited(10),
            aliasLimit = PlanLimit.Limited(10),
            totpLimit = PlanLimit.Limited(10),
            updatedAt = Clock.System.now().epochSeconds
        )
        val DEFAULT_USER_ID = UserId("default-user-id")
    }
}
