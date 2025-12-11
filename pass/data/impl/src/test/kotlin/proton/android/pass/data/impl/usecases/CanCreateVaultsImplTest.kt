/*
 * Copyright (c) 2025 Proton AG
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.Some
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.data.fakes.usecases.FakeObserveVaults
import proton.android.pass.data.fakes.usecases.organizations.FakeObserveOrganizationVaultsPolicy
import proton.android.pass.data.impl.usecases.capabilities.CanCreateVaultImpl
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.organizations.OrganizationVaultCreateMode
import proton.android.pass.domain.organizations.OrganizationVaultsPolicy
import proton.android.pass.test.FixedClock
import java.util.Date

internal class CanCreateVaultsImplTest {

    private lateinit var instance: CanCreateVaultImpl

    private lateinit var observeOrganizationVaultsPolicy: FakeObserveOrganizationVaultsPolicy
    private lateinit var observeVaults: FakeObserveVaults
    private lateinit var currentUserPlan: FakeGetUserPlan
    private lateinit var clock: FixedClock

    @Before
    fun setup() {
        observeOrganizationVaultsPolicy = FakeObserveOrganizationVaultsPolicy()
        observeVaults = FakeObserveVaults()
        currentUserPlan = FakeGetUserPlan()
        clock = FixedClock()

        instance = CanCreateVaultImpl(
            observeOrganizationVaultsPolicy = observeOrganizationVaultsPolicy,
            observeVaults = observeVaults,
            currentUserPlan = currentUserPlan
        )
    }

    @Test
    fun `check policy=AllUsers + free plan + empty vault can create vault`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(
            organizationVaultsPolicy = Some(
                value = OrganizationVaultsPolicy(
                    vaultCreateMode = OrganizationVaultCreateMode.AllUsers
                )
            )
        )
        observeVaults.sendResult(Result.success(emptyList()))
        setupPlan(planType = freePlan)

        val result = instance().first()
        assertThat(result).isTrue()
    }

    @Test
    fun `check policy=AllUsers + free plan + some vaults (under the limit) can still create vault`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(
            organizationVaultsPolicy = Some(
                value = OrganizationVaultsPolicy(vaultCreateMode = OrganizationVaultCreateMode.AllUsers)
            )
        )
        observeVaults.sendResult(Result.success(List(size = 1) { fakeVault }))
        setupPlan(planType = freePlan)

        val result = instance().first()
        assertThat(result).isTrue()
    }

    @Test
    fun `check policy=AllUsers + free plan + vaults (above the limit) can not create vault anymore`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(
            organizationVaultsPolicy = Some(
                value = OrganizationVaultsPolicy(vaultCreateMode = OrganizationVaultCreateMode.AllUsers)
            )
        )
        observeVaults.sendResult(Result.success(List(size = 2) { fakeVault }))
        setupPlan(planType = freePlan)

        val result = instance().first()
        assertThat(result).isFalse()
    }

    @Test
    fun `check policy=AllUsers + plus plan can create vault`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(
            organizationVaultsPolicy = Some(
                value = OrganizationVaultsPolicy(vaultCreateMode = OrganizationVaultCreateMode.AllUsers)
            )
        )
        observeVaults.sendResult(Result.success(List(size = 100) { fakeVault }))
        setupPlan(planType = plusPlan)

        val result = instance().first()
        assertThat(result).isTrue()
    }

    @Test
    fun `check policy=OnlyOrganizationAdmin with no vault can not create vault`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(
            organizationVaultsPolicy = Some(
                value = OrganizationVaultsPolicy(
                    vaultCreateMode = OrganizationVaultCreateMode.OnlyOrganizationAdmin
                )
            )
        )
        observeVaults.sendResult(Result.success(emptyList()))
        setupPlan(planType = businessPlan)

        val result = instance().first()
        assertThat(result).isFalse()
    }

    @Test
    fun `check policy=OnlyOrgAdminsAndPersonalVault + no vault can create vault`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(
            organizationVaultsPolicy = Some(
                value = OrganizationVaultsPolicy(
                    vaultCreateMode = OrganizationVaultCreateMode.OnlyOrgAdminsAndPersonalVault
                )
            )
        )
        observeVaults.sendResult(Result.success(emptyList()))
        setupPlan(planType = businessPlan)

        val result = instance().first()
        assertThat(result).isTrue()
    }

    @Test
    fun `check policy=OnlyOrgAdminsAndPersonalVault + one owned vault can not create vault anymore`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(
            organizationVaultsPolicy = Some(
                value = OrganizationVaultsPolicy(
                    vaultCreateMode = OrganizationVaultCreateMode.OnlyOrgAdminsAndPersonalVault
                )
            )
        )
        observeVaults.sendResult(Result.success(List(1) { fakeVault }))
        setupPlan(planType = businessPlan)

        val result = instance().first()
        assertThat(result).isFalse()
    }


    @Test
    fun `check policy=OnlyOrgAdminsAndPersonalVault + no owned vault can create vault`() = runTest {
        observeOrganizationVaultsPolicy.emitValue(
            organizationVaultsPolicy = Some(
                value = OrganizationVaultsPolicy(
                    vaultCreateMode = OrganizationVaultCreateMode.OnlyOrgAdminsAndPersonalVault
                )
            )
        )
        observeVaults.sendResult(Result.success(List(10) { fakeNotOwnedVault }))
        setupPlan(planType = businessPlan)

        val result = instance().first()
        assertThat(result).isTrue()
    }


    @Test
    fun `check policy=OnlyOrgAdminsAndPersonalVault + at least one owned plan can not create vault anymore`() =
        runTest {
            observeOrganizationVaultsPolicy.emitValue(
                organizationVaultsPolicy = Some(
                    value = OrganizationVaultsPolicy(
                        vaultCreateMode = OrganizationVaultCreateMode.OnlyOrgAdminsAndPersonalVault
                    )
                )
            )
            observeVaults.sendResult(
                Result.success(
                    buildList {
                        addAll(List(5) { fakeNotOwnedVault })
                        add(fakeVault)
                        addAll(List(5) { fakeNotOwnedVault })
                    }
                )
            )
            setupPlan(planType = businessPlan)

            val result = instance().first()
            assertThat(result).isFalse()
        }


    private fun setupPlan(planType: PlanType) {
        val limit = when (planType) {
            is PlanType.Free -> PlanLimit.Limited(2)
            is PlanType.Paid.Plus -> PlanLimit.Unlimited
            is PlanType.Paid.Business -> PlanLimit.Limited(2) // for business we don't look at the limit
            is PlanType.Unknown -> PlanLimit.Limited(2) // impossible
        }
        val plan = Plan(
            planType = planType,
            hideUpgrade = false,
            vaultLimit = limit,
            aliasLimit = limit,
            totpLimit = limit,
            updatedAt = clock.now().epochSeconds
        )
        currentUserPlan.setResult(value = Result.success(plan))
    }

    companion object {
        private val USER_ID = UserId("UserId")

        private val fakeVault = Vault(
            userId = USER_ID,
            shareId = ShareId("ShareId"),
            vaultId = VaultId("vaultId"),
            name = "Vault",
            createTime = Date(),
            shareFlags = ShareFlags(0),
            isOwned = true
        )

        private val fakeNotOwnedVault = fakeVault.copy(isOwned = false)

        private val freePlan = PlanType.Free("free", "Free")
        private val plusPlan = PlanType.Paid.Plus("plus", "Plus")
        private val businessPlan = PlanType.Paid.Business("business", "Business")
    }
}
