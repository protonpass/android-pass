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

package proton.android.pass.data.impl.fakes

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.local.LocalPlanDataSource

class FakeLocalPlanDataSource : LocalPlanDataSource {

    private val planFlow = testFlow<PlanEntity?>()
    private var storePlanResult: Result<Unit> = Result.success(Unit)

    fun emitPlan(planEntity: PlanEntity?) {
        planFlow.tryEmit(planEntity)
    }

    fun setStorePlanResult(result: Result<Unit>) {
        storePlanResult = result
    }

    override fun observePlan(userId: UserId): Flow<PlanEntity?> = planFlow

    override suspend fun storePlan(planEntity: PlanEntity) {
        storePlanResult.getOrThrow()
    }
}
