/*
 * Copyright (c) 2026 Proton AG
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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.organization.RefreshOrganizationSettings
import proton.android.pass.data.fakes.usecases.FakeObserveUser
import proton.android.pass.data.fakes.usecases.FakeObserveVaults
import proton.android.pass.data.fakes.usecases.organizations.FakeObserveOrganizationVaultsPolicy
import proton.android.pass.data.impl.fakes.FakePlanRepository
import proton.android.pass.data.impl.usecases.capabilities.CanCreateDefaultVaultImpl
import proton.android.pass.data.impl.usecases.capabilities.CanCreateVaultImpl
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.UserId
import proton.android.pass.domain.organizations.OrganizationVaultCreateMode
import proton.android.pass.domain.organizations.OrganizationVaultsPolicy

internal class CanCreateDefaultVaultImplTest {

    private lateinit var observeOrganizationVaultsPolicy: FakeObserveOrganizationVaultsPolicy
    private lateinit var refreshOrganizationSettings: StoringRefreshOrganizationSettings
    private lateinit var planRepository: FakePlanRepository
    private lateinit var instance: CanCreateDefaultVaultImpl

    @Before
    fun setup() {
        observeOrganizationVaultsPolicy = FakeObserveOrganizationVaultsPolicy()
        refreshOrganizationSettings = StoringRefreshOrganizationSettings(observeOrganizationVaultsPolicy)

        val observeVaults = FakeObserveVaults().apply { sendResult(Result.success(emptyList())) }
        planRepository = FakePlanRepository().apply { emitPlan(USER_ID, freePlan) }

        instance = CanCreateDefaultVaultImpl(
            refreshOrganizationSettings = refreshOrganizationSettings,
            canCreateVault = CanCreateVaultImpl(
                observeOrganizationVaultsPolicy = observeOrganizationVaultsPolicy,
                observeVaults = observeVaults,
                planRepository = planRepository,
                observeUser = FakeObserveUser()
            )
        )
    }

    @Test
    fun `waits for the organization settings of a fresh account before deciding`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(None)

        val result = instance(USER_ID)

        assertThat(refreshOrganizationSettings.invocations).containsExactly(USER_ID)
        assertThat(result).isTrue()
    }

    @Test
    fun `still decides when the organization settings refresh fails`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(allUsersPolicy)
        refreshOrganizationSettings.failure = IllegalStateException("offline")

        val result = instance(USER_ID)

        assertThat(result).isTrue()
    }

    @Test
    fun `reports false when the organization settings never load`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(None)
        refreshOrganizationSettings.publishOnRefresh = false

        val result = instance(USER_ID)

        assertThat(result).isFalse()
    }

    @Test
    fun `decides with the plan of the account being synced when the current account could create`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(allUsersPolicy)
        planRepository.emitPlan(OTHER_USER_ID, noVaultsPlan)

        val result = instance(OTHER_USER_ID)

        assertThat(result).isFalse()
    }

    @Test
    fun `decides with the plan of the account being synced when the current account could not create`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(allUsersPolicy)
        planRepository.emitPlan(USER_ID, noVaultsPlan)
        planRepository.emitPlan(OTHER_USER_ID, freePlan)

        val result = instance(OTHER_USER_ID)

        assertThat(result).isTrue()
    }

    private class StoringRefreshOrganizationSettings(
        private val observeOrganizationVaultsPolicy: FakeObserveOrganizationVaultsPolicy
    ) : RefreshOrganizationSettings {

        val invocations = mutableListOf<UserId>()
        var failure: Throwable? = null
        var publishOnRefresh: Boolean = true

        override suspend fun invoke(userId: UserId) {
            invocations.add(userId)
            failure?.let { throw it }
            if (publishOnRefresh) observeOrganizationVaultsPolicy.emitValue(allUsersPolicy)
        }
    }

    private companion object {
        private val USER_ID = UserId("UserId")
        private val OTHER_USER_ID = UserId("OtherUserId")

        private val allUsersPolicy = Some(
            OrganizationVaultsPolicy(vaultCreateMode = OrganizationVaultCreateMode.AllUsers)
        )

        private val freePlan = Plan(
            planType = PlanType.Free("free", "Free"),
            hideUpgrade = false,
            vaultLimit = PlanLimit.Limited(2),
            aliasLimit = PlanLimit.Limited(2),
            totpLimit = PlanLimit.Limited(2),
            updatedAt = 0L
        )

        private val noVaultsPlan = freePlan.copy(vaultLimit = PlanLimit.Limited(0))
    }
}
