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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveUsableVaults
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.canCreate
import proton.android.pass.domain.toPermissions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObserveUsableVaultsImpl @Inject constructor(
    private val getUserPlan: GetUserPlan,
    private val observeVaults: ObserveVaults
) : ObserveUsableVaults {

    override fun invoke(userId: UserId?): Flow<ShareSelection> = getUserPlan(userId)
        .flatMapLatest { userPlan ->
            when (userPlan.planType) {
                is PlanType.Paid,
                is PlanType.Trial -> flowOf(ShareSelection.AllShares)

                is PlanType.Free,
                is PlanType.Unknown -> writeableVaults(userId)
            }
        }

    private fun writeableVaults(userId: UserId?): Flow<ShareSelection> = observeVaults(userId)
        .mapLatest { vaults ->
            val writeableVaults = vaults
                .filter { it.role.toPermissions().canCreate() }
                .map { it.shareId }
            ShareSelection.Shares(writeableVaults)
        }
}
