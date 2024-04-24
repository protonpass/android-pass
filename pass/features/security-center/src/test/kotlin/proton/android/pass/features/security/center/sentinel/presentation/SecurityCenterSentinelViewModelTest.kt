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

package proton.android.pass.features.security.center.sentinel.presentation

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.data.fakes.repositories.FakeSentinelRepository
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.securitycenter.fakes.sentinel.FakeEnableSentinel
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.plans.PlanMother

internal class SecurityCenterSentinelViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var sentinelRepository: FakeSentinelRepository
    private lateinit var enableSentinel: FakeEnableSentinel
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    internal fun setUp() {
        getUserPlan = TestGetUserPlan()
        sentinelRepository = FakeSentinelRepository()
        enableSentinel = FakeEnableSentinel(sentinelRepository)
        snackbarDispatcher = TestSnackbarDispatcher()
    }

    @Test
    internal fun `GIVEN user plan WHEN view model is initialized THEN emits initial state`() = runTest {
        val plan = PlanMother.create()
        getUserPlan.setResult(Result.success(plan))
        val expectedState = SecurityCenterSentinelStateMother.create(planType = plan.planType)

        createViewModel().let { viewModel ->
            viewModel.state.test {
                assertThat(awaitItem()).isEqualTo(expectedState)
            }
        }
    }

    @Test
    internal fun `GIVEN user plan WHEN learn more THEN OnLearnMore event should be emitted`() = runTest {
        val plan = PlanMother.create()
        getUserPlan.setResult(Result.success(plan))
        val expectedState = SecurityCenterSentinelStateMother.create(
            event = SecurityCenterSentinelEvent.OnLearnMore,
            planType = plan.planType
        )

        createViewModel().let { viewModel ->
            viewModel.onLearnMore()

            viewModel.state.test {
                assertThat(awaitItem()).isEqualTo(expectedState)
            }
        }
    }

    @Test
    internal fun `GIVEN user plan WHEN enabling sentinel THEN OnSentinelEnableSuccess event should be emitted`() =
        runTest {
            val plan = PlanMother.create()
            getUserPlan.setResult(Result.success(plan))
            val expectedState = SecurityCenterSentinelStateMother.create(
                event = SecurityCenterSentinelEvent.OnSentinelEnableSuccess,
                planType = plan.planType
            )

            createViewModel().let { viewModel ->
                viewModel.onEnableSentinel()

                viewModel.state.test {
                    assertThat(awaitItem()).isEqualTo(expectedState)
                }
            }
        }

    private fun createViewModel() = SecurityCenterSentinelViewModel(
        enableSentinel = enableSentinel,
        snackbarDispatcher = snackbarDispatcher,
        observeUserPlan = getUserPlan
    )

}
