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

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import proton.android.pass.account.fakes.FakePaymentManager
import proton.android.pass.appconfig.fakes.FakeAppConfig
import proton.android.pass.data.api.ItemCountSummary
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeObserveItemCount
import proton.android.pass.data.fakes.usecases.FakeObserveMFACount
import proton.android.pass.data.fakes.usecases.FakeObserveVaultCount
import proton.android.pass.data.impl.fakes.FakePlanRepository
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.test.domain.UserTestFactory
import proton.android.pass.test.domain.plans.PlanTestFactory

class ObserveUpgradeInfoImplTest {

    @Test
    fun `emits upgrade info from all dependencies`() = runTest {
        val userId = UserId("user-id")
        val harness = ObserveUpgradeInfoHarness()
        harness.paymentManager.isUpgradeAvailable = true
        harness.paymentManager.isSubscriptionAvailable = false
        harness.planRepository.emitPlan(
            userId = userId,
            plan = freePlan(hideUpgrade = false)
        )
        harness.observeMFACount.emitResult(2)
        harness.observeItemCount.sendResult(Result.success(ItemCountSummary.Initial.copy(alias = 11)))
        harness.observeVaultCount.emit(5)

        val result = harness.instance(userId).first()

        assertThat(result.isUpgradeAvailable).isTrue()
        assertThat(result.isSubscriptionAvailable).isFalse()
        assertThat(result.totalTotp).isEqualTo(2)
        assertThat(result.totalAlias).isEqualTo(11)
        assertThat(result.totalVaults).isEqualTo(5)
    }

    @Test
    fun `hides upgrade when plan requests it`() = runTest {
        val userId = UserId("user-id")
        val harness = ObserveUpgradeInfoHarness()
        harness.paymentManager.isUpgradeAvailable = true
        harness.planRepository.emitPlan(
            userId = userId,
            plan = freePlan(hideUpgrade = true)
        )

        val result = harness.instance(userId).first()

        assertThat(result.isUpgradeAvailable).isFalse()
    }

    @Test
    fun `observes current user changes when user id is null`() = runTest {
        val userA = UserId("user-a")
        val userB = UserId("user-b")
        val harness = ObserveUpgradeInfoHarness()

        harness.emitPlan(userA, freePlan(hideUpgrade = false))
        harness.emitPlan(userB, plusPlan())
        harness.sendCurrentUser(userA)

        harness.instance().test {
            val first = awaitItem()
            assertThat(first.plan.planType).isInstanceOf(PlanType.Free::class.java)
            assertThat(first.isUpgradeAvailable).isTrue()

            harness.sendCurrentUser(userB)

            val second = awaitItem()
            assertThat(second.plan.planType).isInstanceOf(PlanType.Paid.Plus::class.java)
            assertThat(second.isUpgradeAvailable).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not emit until plan becomes available`() = runTest {
        val userId = UserId("user-id")
        val harness = ObserveUpgradeInfoHarness()
        harness.emitPlan(userId, null)

        harness.instance(userId).test {
            expectNoEvents()
            harness.emitPlan(userId, freePlan(hideUpgrade = false))
            val item = awaitItem()
            assertThat(item.plan.planType).isInstanceOf(PlanType.Free::class.java)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `explicit user id ignores current user changes`() = runTest {
        val explicitUser = UserId("explicit-user")
        val anotherUser = UserId("another-user")
        val harness = ObserveUpgradeInfoHarness()
        harness.emitPlan(explicitUser, freePlan(hideUpgrade = false))
        harness.emitPlan(anotherUser, plusPlan())
        harness.sendCurrentUser(anotherUser)

        harness.instance(explicitUser).test {
            val first = awaitItem()
            assertThat(first.plan.planType).isInstanceOf(PlanType.Free::class.java)

            harness.sendCurrentUser(UserId("third-user"))
            advanceUntilIdle()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `duplicate current user emissions do not retrigger availability checks`() = runTest {
        val userId = UserId("user-id")
        val harness = ObserveUpgradeInfoHarness()
        harness.emitPlan(userId, freePlan(hideUpgrade = false))
        harness.sendCurrentUser(userId)

        harness.instance().test {
            awaitItem()

            harness.sendCurrentUser(userId)
            advanceUntilIdle()
            expectNoEvents()
            assertThat(harness.paymentManager.subscriptionAvailabilityCalls).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `paid plans always disable upgrade display`() = runTest {
        val userId = UserId("user-id")
        val harness = ObserveUpgradeInfoHarness()
        harness.paymentManager.isUpgradeAvailable = true
        harness.emitPlan(userId, plusPlan())

        val result = harness.instance(userId).first()
        assertThat(result.isUpgradeAvailable).isFalse()
    }

    @Test
    fun `upgrade remains visible when payment manager says unavailable`() = runTest {
        val userId = UserId("user-id")
        val harness = ObserveUpgradeInfoHarness()
        harness.paymentManager.isUpgradeAvailable = false
        harness.emitPlan(userId, freePlan(hideUpgrade = false))

        val result = harness.instance(userId).first()
        assertThat(result.isUpgradeAvailable).isTrue()
    }

    @Test
    fun `distinct until changed suppresses identical emissions`() = runTest {
        val userId = UserId("user-id")
        val harness = ObserveUpgradeInfoHarness()
        harness.emitPlan(userId, freePlan(hideUpgrade = false))
        harness.observeItemCount.sendResult(Result.success(ItemCountSummary.Initial.copy(alias = 5)))

        harness.instance(userId).test {
            val first = awaitItem()
            assertThat(first.totalAlias).isEqualTo(5)

            harness.observeItemCount.sendResult(Result.success(ItemCountSummary.Initial.copy(alias = 5)))
            advanceUntilIdle()
            expectNoEvents()

            harness.observeItemCount.sendResult(Result.success(ItemCountSummary.Initial.copy(alias = 6)))
            val second = awaitItem()
            assertThat(second.totalAlias).isEqualTo(6)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `subscription is false when payment manager fails`() = runTest {
        val userId = UserId("user-id")
        val harness = ObserveUpgradeInfoHarness()
        harness.paymentManager.subscriptionAvailableThrowable = IllegalStateException("not available")
        harness.emitPlan(userId, freePlan(hideUpgrade = false))

        val result = harness.instance(userId).first()
        assertThat(result.isSubscriptionAvailable).isFalse()
        assertThat(result.isUpgradeAvailable).isTrue()
    }

    @Test
    fun `alias count is bounded to Int max`() = runTest {
        val userId = UserId("user-id")
        val harness = ObserveUpgradeInfoHarness()
        harness.emitPlan(userId, freePlan(hideUpgrade = false))
        harness.observeItemCount.sendResult(
            Result.success(ItemCountSummary.Initial.copy(alias = Long.MAX_VALUE))
        )

        val result = harness.instance(userId).first()
        assertThat(result.totalAlias).isEqualTo(Int.MAX_VALUE)
    }

    private class ObserveUpgradeInfoHarness {
        val paymentManager = FakePaymentManager()
        val observeCurrentUser = FakeObserveCurrentUser()
        val observeMFACount = FakeObserveMFACount()
        val observeItemCount = FakeObserveItemCount()
        val observeVaultCount = FakeObserveVaultCount()
        val planRepository = FakePlanRepository()
        private val appConfig = FakeAppConfig()

        private val instance = ObserveUpgradeInfoImpl(
            appConfig = appConfig,
            observeCurrentUser = observeCurrentUser,
            observeMFACount = observeMFACount,
            observeItemCount = observeItemCount,
            paymentManager = paymentManager,
            planRepository = planRepository,
            observeVaultCount = observeVaultCount
        )

        init {
            observeCurrentUser.sendUser(UserTestFactory.create(userId = UserId("default-user")))
            observeMFACount.emitResult(0)
            observeItemCount.sendResult(Result.success(ItemCountSummary.Initial))
            observeVaultCount.emit(0)
        }

        fun instance(userId: UserId? = null): Flow<UpgradeInfo> = instance.invoke(userId)

        fun sendCurrentUser(userId: UserId) {
            observeCurrentUser.sendUser(UserTestFactory.create(userId = userId))
        }

        fun emitPlan(userId: UserId, plan: Plan?) {
            planRepository.emitPlan(userId, plan)
        }
    }

    companion object {
        private fun freePlan(hideUpgrade: Boolean): Plan = PlanTestFactory.create(
            planType = PlanType.Free(name = "free", displayName = "Free"),
            hideUpgrade = hideUpgrade,
            vaultLimit = PlanLimit.Limited(2),
            aliasLimit = PlanLimit.Limited(10),
            totpLimit = PlanLimit.Limited(2),
            updatedAt = 0
        )

        private fun plusPlan(): Plan = PlanTestFactory.create(
            planType = PlanType.Paid.Plus(name = "plus", displayName = "Plus"),
            hideUpgrade = false
        )
    }
}
