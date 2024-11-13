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

import android.os.Build
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.appconfig.fakes.TestAppConfig
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.fakes.TestAutofillManager
import proton.android.pass.common.api.some
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveInvites
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginSyncStatus
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.Autofill
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.Invite
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.NotificationPermission
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipPage.Trial
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipsUiState
import proton.android.pass.featurehome.impl.onboardingtips.OnBoardingTipsViewModel
import proton.android.pass.notifications.fakes.TestNotificationManager
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.HasDismissedNotificationBanner
import proton.android.pass.preferences.HasDismissedSLSyncBanner
import proton.android.pass.preferences.HasDismissedTrialBanner
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestPendingInvite
import proton.android.pass.test.domain.TestVault

class OnBoardingTipsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: OnBoardingTipsViewModel
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var autofillManager: TestAutofillManager
    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var observeInvites: TestObserveInvites
    private lateinit var notificationManager: TestNotificationManager
    private lateinit var appConfig: TestAppConfig
    private lateinit var featureFlagsPreferencesRepository: TestFeatureFlagsPreferenceRepository
    private lateinit var observeSimpleLoginSyncStatus: FakeObserveSimpleLoginSyncStatus

    @Before
    fun setUp() {
        preferenceRepository = TestPreferenceRepository()
        autofillManager = TestAutofillManager()
        getUserPlan = TestGetUserPlan()
        observeInvites = TestObserveInvites()
        notificationManager = TestNotificationManager()
        appConfig = TestAppConfig()
        featureFlagsPreferencesRepository = TestFeatureFlagsPreferenceRepository()
        observeSimpleLoginSyncStatus = FakeObserveSimpleLoginSyncStatus()
        viewModel = OnBoardingTipsViewModel(
            autofillManager = autofillManager,
            preferencesRepository = preferenceRepository,
            observeInvites = observeInvites,
            getUserPlan = getUserPlan,
            notificationManager = notificationManager,
            appConfig = appConfig,
            featureFlagsPreferencesRepository = featureFlagsPreferencesRepository,
            observeSimpleLoginSyncStatus = observeSimpleLoginSyncStatus
        )
    }

    @Test
    fun `Should not show banner if autofill is unsupported`() = runTest {
        setupPlan()
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should not show banner if autofill is enabled by our service`() = runTest {
        setupPlan()
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should show banner if autofill is enabled by other service`() = runTest {
        setupPlan()
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(Autofill.some()))
        }
    }

    @Test
    fun `Should show banner if autofill is disabled `() = runTest {
        setupPlan()
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(Autofill.some()))
        }
    }

    @Test
    fun `Should not show banner if autofill banner has been dismised`() = runTest {
        setupPlan()
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should display trial banner if plan is trial`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        preferenceRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.NotDismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(Trial.some()))
        }
    }

    @Test
    fun `Should display autofill banner when trial is dismissed`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        preferenceRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.NotDismissed)

        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(Trial.some()))

            viewModel.onDismiss(Trial)

            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(Autofill.some()))
        }
    }

    @Test
    fun `Should display invite banner regardless of the state of the other conditions`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        preferenceRepository.setHasDismissedTrialBanner(HasDismissedTrialBanner.NotDismissed)
        observeInvites.emitInvites(listOf(TestPendingInvite.create()))

        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(Invite.some()))
        }
    }

    @Test
    fun `Should display notification permission banner if not notification permission and banner not dismissed`() =
        runTest {
            setupPlan(PlanType.Trial("", "", 1))
            setupSyncStatus()
            notificationManager.setHasNotificationPermission(false)
            viewModel.onNotificationPermissionChanged(false)
            preferenceRepository.setHasDismissedNotificationBanner(HasDismissedNotificationBanner.NotDismissed)
            appConfig.setAndroidVersion(Build.VERSION_CODES.TIRAMISU)

            viewModel.stateFlow.test {
                assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(NotificationPermission.some()))
            }
        }

    @Test
    fun `Should not display notification permission if has permission and banner not dismissed`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        setupSyncStatus()
        notificationManager.setHasNotificationPermission(true)
        viewModel.onNotificationPermissionChanged(true)
        preferenceRepository.setHasDismissedNotificationBanner(HasDismissedNotificationBanner.NotDismissed)
        appConfig.setAndroidVersion(Build.VERSION_CODES.TIRAMISU)

        viewModel.stateFlow.test {
            assertThat(awaitItem().tipToShow).isNotEqualTo(NotificationPermission)
        }
    }

    @Test
    fun `Should not display notification permission if not permission and banner dismissed`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        setupSyncStatus()
        notificationManager.setHasNotificationPermission(false)
        viewModel.onNotificationPermissionChanged(false)
        preferenceRepository.setHasDismissedNotificationBanner(HasDismissedNotificationBanner.Dismissed)
        appConfig.setAndroidVersion(Build.VERSION_CODES.TIRAMISU)

        viewModel.stateFlow.test {
            assertThat(awaitItem().tipToShow).isNotEqualTo(NotificationPermission)
        }
    }

    @Test
    fun `Should not display notification permission if not permission and banner not dismissed but version LT 13`() =
        runTest {
            setupPlan(PlanType.Trial("", "", 1))
            setupSyncStatus()
            notificationManager.setHasNotificationPermission(false)
            viewModel.onNotificationPermissionChanged(false)
            preferenceRepository.setHasDismissedNotificationBanner(HasDismissedNotificationBanner.Dismissed)
            appConfig.setAndroidVersion(Build.VERSION_CODES.BASE)

            viewModel.stateFlow.test {
                assertThat(awaitItem().tipToShow).isNotEqualTo(NotificationPermission)
            }
        }

    @Test
    fun `Should not display SL sync if banner dismissed`() = runTest {
        setupPlan(PlanType.Trial("", "", 1))
        setupSyncStatus()
        preferenceRepository.setHasDismissedSLSyncBanner(HasDismissedSLSyncBanner.Dismissed)

        viewModel.stateFlow.test {
            assertThat(awaitItem().tipToShow).isNotEqualTo(NotificationPermission)
        }
    }

    private fun setupPlan(planType: PlanType = PlanType.Paid.Plus("", "")) {
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

    private fun setupSyncStatus() {
        val syncStatus = SimpleLoginSyncStatus(
            isSyncEnabled = false,
            isPreferenceEnabled = false,
            pendingAliasCount = 0,
            defaultVault = TestVault.create(),
            canManageAliases = false
        )
        observeSimpleLoginSyncStatus.updateSyncStatus(syncStatus)
    }
}
