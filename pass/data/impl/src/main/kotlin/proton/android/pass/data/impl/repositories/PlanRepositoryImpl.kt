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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.UserAccessDataRepository
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.local.LocalPlanDataSource
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import javax.inject.Inject

class PlanRepositoryImpl @Inject constructor(
    private val localPlanDataSource: LocalPlanDataSource,
    private val userAccessDataRepository: UserAccessDataRepository
) : PlanRepository {

    override fun observePlan(userId: UserId): Flow<Plan?> = localPlanDataSource.observePlan(userId)
        .map { planEntity -> planEntity?.toPlan() }

    override suspend fun refreshPlan(userId: UserId) {
        userAccessDataRepository.refresh(userId)
    }

    private fun PlanEntity.toPlan(): Plan = Plan(
        planType = toPlanType(),
        vaultLimit = toPlanLimit(vaultLimit),
        aliasLimit = toPlanLimit(aliasLimit),
        totpLimit = toPlanLimit(totpLimit),
        updatedAt = updatedAt,
        hideUpgrade = hideUpgrade
    )

    private fun PlanEntity.toPlanType(): PlanType = when (type) {
        PlanType.PLAN_NAME_FREE -> PlanType.Free(
            name = internalName,
            displayName = displayName
        )

        PlanType.PLAN_NAME_PLUS -> PlanType.Paid.Plus(
            name = internalName,
            displayName = displayName
        )

        PlanType.PLAN_NAME_BUSINESS -> PlanType.Paid.Business(
            name = internalName,
            displayName = displayName
        )

        else -> PlanType.Unknown(
            name = internalName,
            displayName = displayName
        )
    }

    private fun toPlanLimit(value: Int): PlanLimit = if (value == -1) {
        PlanLimit.Unlimited
    } else {
        PlanLimit.Limited(value)
    }

}
