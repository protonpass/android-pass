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

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.capabilities.CanCreateItemInVault
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.pass.domain.Plan
import proton.pass.domain.PlanType
import proton.pass.domain.Share
import proton.pass.domain.Vault
import proton.pass.domain.canCreate
import proton.pass.domain.toPermissions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CanCreateItemInVaultImpl @Inject constructor(
    private val featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository,
    private val observeShares: ObserveAllShares,
    private val getUserPlan: GetUserPlan
) : CanCreateItemInVault {

    override suspend fun invoke(vault: Vault): CanCreateItemInVault.CanCreateResult {
        val permissions = vault.role.toPermissions()
        if (!permissions.canCreate()) {
            return CanCreateItemInVault.CanCreateResult.CannotCreate(
                CanCreateItemInVault.CanCreateResult.Reason.NoCreatePermission
            )
        }

        val data = runCatching { getData() }.getOrElse {
            PassLogger.w(TAG, it, "Error while checking if item can be created in vault")
            return CanCreateItemInVault.CanCreateResult.CannotCreate(
                CanCreateItemInVault.CanCreateResult.Reason.Unknown
            )
        }

        return when (data.userPlan.planType) {
            is PlanType.Paid, is PlanType.Trial -> CanCreateItemInVault.CanCreateResult.CanCreate
            else -> {
                if (data.removeFeatureFlagEnabled) {
                    checkWithoutPrimaryVault(vault, data)
                } else {
                    checkWithPrimaryVault(vault)
                }
            }
        }
    }

    private fun checkWithoutPrimaryVault(
        vault: Vault,
        data: Data
    ): CanCreateItemInVault.CanCreateResult {
        // We are sure that the user is not paid
        // They can only create in the N oldest vaults
        val twoOldestVaults = data.allVaults
            .sortedBy { it.createTime }
            .take(OLDEST_VAULTS_FOR_FREE)
        if (twoOldestVaults.isEmpty()) {
            PassLogger.w(TAG, "No vault found")
            return CanCreateItemInVault.CanCreateResult.CannotCreate(
                CanCreateItemInVault.CanCreateResult.Reason.Unknown
            )
        }

        val oldestVaultIds = twoOldestVaults.map { it.id }
        return if (oldestVaultIds.contains(vault.shareId)) {
            CanCreateItemInVault.CanCreateResult.CanCreate
        } else {
            CanCreateItemInVault.CanCreateResult.CannotCreate(
                CanCreateItemInVault.CanCreateResult.Reason.Downgraded
            )
        }
    }

    // We are sure that the user is not paid
    // They can only create in the primary vault
    private fun checkWithPrimaryVault(vault: Vault): CanCreateItemInVault.CanCreateResult =
        if (vault.isPrimary) {
            CanCreateItemInVault.CanCreateResult.CanCreate
        } else {
            CanCreateItemInVault.CanCreateResult.CannotCreate(
                CanCreateItemInVault.CanCreateResult.Reason.Downgraded
            )
        }

    @Suppress("UNCHECKED_CAST")
    private suspend fun getData(): Data = withContext(Dispatchers.IO) {
        val ff = async {
            featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.REMOVE_PRIMARY_VAULT).first()
        }
        val allVaults = async { observeShares().first() }
        val plan = async { getUserPlan().first() }

        val contents = awaitAll(ff, allVaults, plan)
        Data(
            removeFeatureFlagEnabled = contents[0] as Boolean,
            allVaults = contents[1] as List<Share>,
            userPlan = contents[2] as Plan
        )
    }

    private data class Data(
        val removeFeatureFlagEnabled: Boolean,
        val allVaults: List<Share>,
        val userPlan: Plan
    )

    companion object {
        private const val TAG = "CanCreateItemInVaultImpl"

        private const val OLDEST_VAULTS_FOR_FREE = 2
    }
}
