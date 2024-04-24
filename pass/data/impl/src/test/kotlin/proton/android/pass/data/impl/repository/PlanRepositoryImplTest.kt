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

package proton.android.pass.data.impl.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.impl.db.entities.PlanEntity
import proton.android.pass.data.impl.fakes.TestLocalPlanDataSource
import proton.android.pass.data.impl.fakes.TestLocalUserAccessDataDataSource
import proton.android.pass.data.impl.fakes.TestRemotePlanDataSource
import proton.android.pass.data.impl.repositories.PlanRepositoryImpl
import proton.android.pass.data.impl.responses.AccessResponse
import proton.android.pass.data.impl.responses.MonitorResponse
import proton.android.pass.data.impl.responses.PlanResponse
import proton.android.pass.data.impl.responses.UserAccessResponse
import proton.android.pass.domain.PlanType
import proton.android.pass.test.FixedClock
import kotlin.time.Duration.Companion.days

internal class PlanRepositoryImplTest {

    private lateinit var instance: PlanRepositoryImpl
    private lateinit var local: TestLocalPlanDataSource
    private lateinit var remote: TestRemotePlanDataSource
    private lateinit var clock: FixedClock
    private lateinit var localUserAccessDataDataSource: TestLocalUserAccessDataDataSource

    @Before
    fun setup() {
        local = TestLocalPlanDataSource()
        remote = TestRemotePlanDataSource()
        clock = FixedClock(Clock.System.now())
        localUserAccessDataDataSource = TestLocalUserAccessDataDataSource()
        instance = PlanRepositoryImpl(
            remotePlanDataSource = remote,
            localPlanDataSource = local,
            clock = clock,
            localUserAccessDataDataSource = localUserAccessDataDataSource
        )
    }

    @Test
    fun `sendUserAccessAndObservePlan return trial if trial is not expired and not paid`() = runTest {
        val future = clock.now().plus(2.days)
        local.emitPlan(planEntity(trialEnd = future))

        val plan = instance.sendUserAccessAndObservePlan(USER_ID, false).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Trial::class.java)
    }

    @Test
    fun `sendUserAccessAndObservePlan return paid if trial is not expired and paid`() = runTest {
        val future = clock.now().plus(2.days)
        local.emitPlan(planEntity(type = PlanType.PLAN_NAME_PLUS, trialEnd = future))

        val plan = instance.sendUserAccessAndObservePlan(USER_ID, false).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Trial::class.java)
    }

    @Test
    fun `sendUserAccessAndObservePlan return free if trial is expired and is not paid`() = runTest {
        val past = clock.now().minus(2.days)
        local.emitPlan(planEntity(type = PlanType.PLAN_NAME_FREE, trialEnd = past))

        val plan = instance.sendUserAccessAndObservePlan(USER_ID, false).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Free::class.java)
    }

    @Test
    fun `sendUserAccessAndObservePlan return paid if trial is expired and is paid`() = runTest {
        val past = clock.now().minus(2.days)
        local.emitPlan(planEntity(type = PlanType.PLAN_NAME_PLUS, trialEnd = past))

        val plan = instance.sendUserAccessAndObservePlan(USER_ID, false).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Paid::class.java)
    }

    @Test
    fun `sendUserAccessAndObservePlan return unknown if trial is expired and is unknown`() = runTest {
        val past = clock.now().minus(2.days)
        local.emitPlan(planEntity(type = "unknown", trialEnd = past))

        val plan = instance.sendUserAccessAndObservePlan(USER_ID, false).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Unknown::class.java)
    }

    @Test
    fun `sendUserAccessAndObservePlan return unknown if trial is not expired and is unknown`() = runTest {
        val future = clock.now().plus(2.days)
        local.emitPlan(planEntity(type = "unknown", trialEnd = future))

        val plan = instance.sendUserAccessAndObservePlan(USER_ID, false).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Unknown::class.java)
    }


    @Test
    fun `observePlan return trial if trial is not expired and not paid`() = runTest {
        val future = clock.now().plus(2.days)
        local.emitPlan(planEntity(trialEnd = future))

        val plan = instance.observePlan(USER_ID).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Trial::class.java)
    }

    @Test
    fun `observePlan return paid if trial is not expired and paid`() = runTest {
        val future = clock.now().plus(2.days)
        local.emitPlan(planEntity(type = PlanType.PLAN_NAME_PLUS, trialEnd = future))

        val plan = instance.observePlan(USER_ID).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Trial::class.java)
    }

    @Test
    fun `observePlan return free if trial is expired and is not paid`() = runTest {
        val past = clock.now().minus(2.days)
        local.emitPlan(planEntity(type = PlanType.PLAN_NAME_FREE, trialEnd = past))

        val plan = instance.observePlan(USER_ID).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Free::class.java)
    }

    @Test
    fun `observePlan return paid if trial is expired and is paid`() = runTest {
        val past = clock.now().minus(2.days)
        local.emitPlan(planEntity(type = PlanType.PLAN_NAME_PLUS, trialEnd = past))

        val plan = instance.observePlan(USER_ID).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Paid::class.java)
    }

    @Test
    fun `observePlan return unknown if trial is expired and is unknown`() = runTest {
        val past = clock.now().minus(2.days)
        local.emitPlan(planEntity(type = "unknown", trialEnd = past))

        val plan = instance.observePlan(USER_ID).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Unknown::class.java)
    }

    @Test
    fun `observePlan return unknown if trial is not expired and is unknown`() = runTest {
        val future = clock.now().plus(2.days)
        local.emitPlan(planEntity(type = "unknown", trialEnd = future))

        val plan = instance.observePlan(USER_ID).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Unknown::class.java)
    }

    @Test
    fun `refresh updates localUserAccessDataSource`() = runTest {
        local.emitPlan(planEntity(type = PlanType.PLAN_NAME_PLUS, trialEnd = null))


        val pendingInvites = 2
        val waitingNewUserInvites = 3
        val userAccessResponse = UserAccessResponse(
            code = 1000,
            accessResponse = AccessResponse(
                planResponse = PlanResponse(
                    type = "",
                    internalName = "",
                    displayName = "",
                    vaultLimit = null,
                    aliasLimit = null,
                    totpLimit = null,
                    hideUpgrade = false,
                    trialEnd = null
                ),
                pendingInvites = pendingInvites,
                waitingNewUserInvites = waitingNewUserInvites,
                minVersionUpgrade = null,
                monitorResponse = MonitorResponse(
                    protonMonitorEnabled = false, aliasMonitorEnabled = false
                )
            )
        )
        remote.setResult(Result.success(userAccessResponse))

        val plan = instance.sendUserAccessAndObservePlan(USER_ID, forceRefresh = true).first()
        assertThat(plan.planType).isInstanceOf(PlanType.Paid::class.java)

        val userAccessData = localUserAccessDataDataSource.observe(USER_ID).first()
        assertThat(userAccessData).isNotNull()
        assertThat(userAccessData!!.pendingInvites).isEqualTo(pendingInvites)
        assertThat(userAccessData.waitingNewUserInvites).isEqualTo(waitingNewUserInvites)
    }

    @Test
    internal fun `GIVEN business plan WHEN observing plan THEN return business plan`() = runTest {
        val planType = "business"
        val expectedPlan = PlanType.Paid.Business(
            name = planType,
            displayName = planType
        )
        local.emitPlan(planEntity(type = planType, trialEnd = null))

        val plan = instance.observePlan(USER_ID).first()

        assertThat(plan.planType).isEqualTo(expectedPlan)
    }

    private fun planEntity(type: String = PlanType.PLAN_NAME_FREE, trialEnd: Instant?) = PlanEntity(
        userId = USER_ID.id,
        type = type,
        internalName = type,
        displayName = type,
        vaultLimit = 1,
        aliasLimit = 1,
        totpLimit = 1,
        updatedAt = clock.now().epochSeconds,
        hideUpgrade = false,
        trialEnd = trialEnd?.epochSeconds
    )

    companion object {
        private val USER_ID = UserId("123")
    }

}
