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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.Vault

class ObserveUsableVaultsImplTest {

    private lateinit var instance: ObserveUsableVaultsImpl

    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var observeVaults: TestObserveVaults

    @Before
    fun setup() {
        getUserPlan = TestGetUserPlan()
        observeVaults = TestObserveVaults()

        instance = ObserveUsableVaultsImpl(
            getUserPlan = getUserPlan,
            observeVaults = observeVaults
        )
    }

    @Test
    fun `only writeable vaults if plan is free`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setPlan(PlanType.Free("", ""))
        setVaults(
            mapOf(
                vault1 to ShareRole.Write,
                vault2 to ShareRole.Write,
                vault3 to ShareRole.Read
            )
        )

        val res = instance().first()
        assertThat(res).isEqualTo(ShareSelection.Shares(listOf(vault1, vault2)))
    }

    @Test
    fun `only writeable vaults if plan is unknown`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setPlan(PlanType.Unknown("", ""))
        setVaults(
            mapOf(
                vault1 to ShareRole.Write,
                vault2 to ShareRole.Write,
                vault3 to ShareRole.Read
            )
        )

        val res = instance().first()
        assertThat(res).isEqualTo(ShareSelection.Shares(listOf(vault1, vault2)))
    }

    @Test
    fun `all vaults if plan is plus`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setPlan(PlanType.Paid.Plus("", ""))
        setVaults(
            mapOf(
                vault1 to ShareRole.Write,
                vault2 to ShareRole.Write,
                vault3 to ShareRole.Read
            )
        )

        val res = instance().first()
        assertThat(res).isEqualTo(ShareSelection.AllShares)
    }

    @Test
    fun `all vaults if plan is business`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setPlan(PlanType.Paid.Business("", ""))
        setVaults(
            mapOf(
                vault1 to ShareRole.Write,
                vault2 to ShareRole.Write,
                vault3 to ShareRole.Read
            )
        )

        val res = instance().first()
        assertThat(res).isEqualTo(ShareSelection.AllShares)
    }

    @Test
    fun `all vaults if plan is trial`() = runTest {
        val vault1 = ShareId("1")
        val vault2 = ShareId("2")
        val vault3 = ShareId("3")

        setPlan(PlanType.Trial("", "", remainingDays = 1))
        setVaults(
            mapOf(
                vault1 to ShareRole.Write,
                vault2 to ShareRole.Write,
                vault3 to ShareRole.Read
            )
        )

        val res = instance().first()
        assertThat(res).isEqualTo(ShareSelection.AllShares)
    }

    private fun setVaults(vaults: Map<ShareId, ShareRole>) {
        val vaultInstances: List<Vault> = vaults.map { (shareId, role) ->
            Vault(
                shareId = shareId,
                role = role,
                name = "unused"
            )
        }

        observeVaults.sendResult(Result.success(vaultInstances))
    }

    private fun setPlan(planType: PlanType) {
        val plan = Plan(
            planType = planType,
            hideUpgrade = false,
            vaultLimit = PlanLimit.Limited(1),
            aliasLimit = PlanLimit.Limited(1),
            totpLimit = PlanLimit.Limited(1),
            updatedAt = Clock.System.now().epochSeconds
        )
        getUserPlan.setResult(Result.success(plan))
    }

}
