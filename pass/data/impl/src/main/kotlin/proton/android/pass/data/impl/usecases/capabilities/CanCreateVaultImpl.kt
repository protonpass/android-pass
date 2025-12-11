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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.data.api.usecases.capabilities.CanCreateVault
import proton.android.pass.data.api.usecases.organization.ObserveOrganizationVaultsPolicy
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.Vault
import proton.android.pass.domain.organizations.OrganizationVaultCreateMode
import proton.android.pass.domain.organizations.OrganizationVaultsPolicy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanCreateVaultImpl @Inject constructor(
    private val observeOrganizationVaultsPolicy: ObserveOrganizationVaultsPolicy,
    private val observeVaults: ObserveVaults,
    private val currentUserPlan: GetUserPlan
) : CanCreateVault {

    override fun invoke(): Flow<Boolean> = combine(
        observeOrganizationVaultsPolicy(),
        observeVaults(includeHidden = true),
        currentUserPlan(),
        ::transformVaultCheck
    )

    private fun transformVaultCheck(
        organizationVaultsPolicyOption: Option<OrganizationVaultsPolicy>,
        vaults: List<Vault>,
        plan: Plan
    ): Boolean {
        return when (organizationVaultsPolicyOption) {
            None -> false
            is Some -> {
                when (organizationVaultsPolicyOption.value.vaultCreateMode) {
                    OrganizationVaultCreateMode.AllUsers -> {
                        when (val limit = plan.vaultLimit) {
                            PlanLimit.Unlimited -> true
                            is PlanLimit.Limited -> vaults.size < limit.limit
                        }
                    }

                    OrganizationVaultCreateMode.OnlyOrganizationAdmin -> false
                    OrganizationVaultCreateMode.OnlyOrgAdminsAndPersonalVault -> {
                        vaults.none { it.isOwned }
                    }
                }
            }
        }
    }
}
