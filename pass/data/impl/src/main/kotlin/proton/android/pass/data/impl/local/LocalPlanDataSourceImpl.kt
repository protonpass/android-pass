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
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.responses.PlanResponse
import javax.inject.Inject

class LocalPlanDataSourceImpl @Inject constructor(
    private val database: PassDatabase,
    private val clock: Clock
) : LocalPlanDataSource {

    override fun observePlan(userId: UserId): Flow<PlanEntity> =
        database.planDao().observeUserPlan(userId.id)

    override suspend fun storePlan(userId: UserId, planResponse: PlanResponse) {
        val entity = PlanEntity(
            userId = userId.id,
            vaultLimit = planResponse.vaultLimit ?: -1,
            aliasLimit = planResponse.aliasLimit ?: -1,
            totpLimit = planResponse.totpLimit ?: -1,
            type = planResponse.type,
            internalName = planResponse.internalName,
            displayName = planResponse.displayName,
            hideUpgrade = planResponse.hideUpgrade,
            trialEnd = planResponse.trialEnd,
            updatedAt = clock.now().epochSeconds
        )
        database.planDao().insertOrUpdate(entity)
    }
}
