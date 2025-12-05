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
import proton.android.pass.data.fakes.usecases.FakeGetUserPlan
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.securitycenter.fakes.sentinel.FakeDisableSentinel
import proton.android.pass.securitycenter.fakes.sentinel.FakeEnableSentinel
import proton.android.pass.securitycenter.fakes.sentinel.FakeObserveCanEnableSentinel
import proton.android.pass.securitycenter.fakes.sentinel.FakeObserveIsSentinelEnabled
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.plans.PlanTestFactory
import proton.android.pass.test.domain.plans.PlanTypeTestFactory

internal class SecurityCenterSentinelViewModelTest {

    @get:Rule
    internal val dispatcherRule = MainDispatcherRule()

    private lateinit var observeIsSentinelEnabled: FakeObserveIsSentinelEnabled
    private lateinit var observeCanEnableSentinel: FakeObserveCanEnableSentinel
    private lateinit var getUserPlan: FakeGetUserPlan
    private lateinit var sentinelRepository: FakeSentinelRepository
    private lateinit var enableSentinel: FakeEnableSentinel
    private lateinit var disableSentinel: FakeDisableSentinel
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher

    @Before
    internal fun setUp() {
        sentinelRepository = FakeSentinelRepository()
        observeIsSentinelEnabled = FakeObserveIsSentinelEnabled(sentinelRepository)
        enableSentinel = FakeEnableSentinel(sentinelRepository)
        disableSentinel = FakeDisableSentinel(sentinelRepository)
        getUserPlan = FakeGetUserPlan()
        observeCanEnableSentinel = FakeObserveCanEnableSentinel(sentinelRepository)
        snackbarDispatcher = FakeSnackbarDispatcher()
    }

    @Test
    internal fun `WHEN view model is initialized THEN emits initial state`() = runTest {
        val planType = PlanTypeTestFactory.Free.create(
            name = "free",
            displayName = "Proton Free"
        )
        val expectedState = SecurityCenterSentinelStateTestFactory.create(
            planType = planType
        )

        createViewModel().let { viewModel ->
            viewModel.state.test {
                assertThat(awaitItem()).isEqualTo(expectedState)
            }
        }
    }

    @Test
    internal fun `WHEN learn more THEN emits OnLearnMore event`() = runTest {
        val plan = PlanTestFactory.create()
        getUserPlan.setResult(Result.success(plan))
        val expectedState = SecurityCenterSentinelStateTestFactory.create(
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
    internal fun `WHEN enabling sentinel THEN emits OnSentinelEnableSuccess event`() = runTest {
        val plan = PlanTestFactory.create()
        getUserPlan.setResult(Result.success(plan))
        val expectedState = SecurityCenterSentinelStateTestFactory.create(
            isSentinelEnabled = true,
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

    @Test
    internal fun `WHEN disabling sentinel THEN emits OnSentinelDisableSuccess event`() = runTest {
        val plan = PlanTestFactory.create()
        getUserPlan.setResult(Result.success(plan))
        val expectedState = SecurityCenterSentinelStateTestFactory.create(
            isSentinelEnabled = false,
            event = SecurityCenterSentinelEvent.OnSentinelDisableSuccess,
            planType = plan.planType
        )

        createViewModel().let { viewModel ->
            viewModel.onDisableSentinel()

            viewModel.state.test {
                assertThat(awaitItem()).isEqualTo(expectedState)
            }
        }
    }

    private fun createViewModel() = SecurityCenterSentinelViewModel(
        observeIsSentinelEnabled = observeIsSentinelEnabled,
        enableSentinel = enableSentinel,
        disableSentinel = disableSentinel,
        snackbarDispatcher = snackbarDispatcher,
        observeUserPlan = getUserPlan,
        observeCanEnableSentinel = observeCanEnableSentinel
    )

}
