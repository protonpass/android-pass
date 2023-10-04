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

package proton.android.pass.data.impl.usecases.capabilities

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.pass.domain.PlanLimit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanCreateVaultImpl @Inject constructor(
    observeVaults: ObserveVaults,
    currentUserPlan: GetUserPlan
) : CanCreateVault {

    private val canCreateVaultFlow: Flow<Boolean> = combine(
        observeVaults(),
        currentUserPlan()
    ) { vaults, plan ->
        when (val vaultLimit = plan.vaultLimit) {
            PlanLimit.Unlimited -> true
            is PlanLimit.Limited -> vaults.size < vaultLimit.limit
        }
    }


    override fun invoke(): Flow<Boolean> = canCreateVaultFlow
}
