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

package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.PlanEntity
import javax.inject.Inject

class LocalPlanDataSourceImpl @Inject constructor(
    private val database: PassDatabase
) : LocalPlanDataSource {

    override fun observePlan(userId: UserId): Flow<PlanEntity?> = database.planDao().observeUserPlan(userId.id)

    override suspend fun storePlan(planEntity: PlanEntity) {
        database.planDao().insertOrUpdate(planEntity)
    }

}
