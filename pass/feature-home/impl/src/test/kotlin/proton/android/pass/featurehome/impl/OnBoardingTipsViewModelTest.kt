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

package proton.android.pass.featurehome.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.fakes.TestAutofillManager
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveInvites
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.AUTOFILL
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.INVITE
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.TRIAL
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipsUiState
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipsViewModel
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.HasDismissedTrialBanner
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestPendingInvite
import proton.pass.domain.Plan
import proton.pass.domain.PlanLimit
import proton.pass.domain.PlanType

class OnBoardingTipsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: OnBoardingTipsViewModel
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var autofillManager: TestAutofillManager
    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var observeInvites: TestObserveInvites

    @Before
    fun setUp() {
        preferenceRepository = TestPreferenceRepository()
        autofillManager = TestAutofillManager()
        getUserPlan = TestGetUserPlan()
        observeInvites = TestObserveInvites()
    }

    @Test
    fun `Should not show banner if autofill is unsupported`() = runTest {
        setupPlan()
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(
            autofillManager,
            preferenceRepository,
            observeInvites,
            getUserPlan
        )
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should not show banner if autofill is enabled by our service`() = runTest {
        setupPlan()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(
            autofillManager,
            preferenceRepository,
            observeInvites,
            getUserPlan
        )
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should show banner if autofill is enabled by other service`() = runTest {
        setupPlan()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(
            autofillManager,
            preferenceRepository,
            observeInvites,
            getUserPlan
        )
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(AUTOFILL)))
        }
    }

    @Test
    fun `Should show banner if autofill is disabled `() = runTest {
        setupPlan()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(
            autofillManager,
            preferenceRepository,
            observeInvites,
            getUserPlan
        )
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(AUTOFILL)))
        }
    }

    @Test
    fun `Should not show banner if autofill banner has been dismised`() = runTest {
        setupPlan()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)
        viewModel = OnBoardingTipsViewModel(
            autofillManager,
            preferenceRepository,
            observeInvites,
            getUserPlan
        )
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should display trial banner if plan is trial`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        preferenceRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(
            autofillManager,
            preferenceRepository,
            observeInvites,
            getUserPlan
        )
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(TRIAL)))
        }
    }

    @Test
    fun `Should display autofill banner when trial is dismissed`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        preferenceRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.NotDismissed)
        viewModel = OnBoardingTipsViewModel(
            autofillManager,
            preferenceRepository,
            observeInvites,
            getUserPlan
        )

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(TRIAL)))

            viewModel.onDismiss(TRIAL)

            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(AUTOFILL)))
        }
    }

    @Test
    fun `Should display invite banner regardless of the state of the other conditions`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        preferenceRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.NotDismissed)
        observeInvites.emitInvites(listOf(TestPendingInvite.create()))
        viewModel = OnBoardingTipsViewModel(
            autofillManager,
            preferenceRepository,
            observeInvites,
            getUserPlan
        )

        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(persistentSetOf(INVITE)))
        }
    }

    private fun setupPlan(
        planType: PlanType = PlanType.Paid("", "")
    ) {
        val plan = Plan(
            planType = planType,
            hideUpgrade = false,
            vaultLimit = PlanLimit.Unlimited,
            aliasLimit = PlanLimit.Unlimited,
            totpLimit = PlanLimit.Unlimited,
            updatedAt = 123
        )
        getUserPlan.setResult(Result.success(plan))
    }
}
