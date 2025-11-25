/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.home

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
import proton.android.pass.data.fakes.usecases.TestObserveInvites
import proton.android.pass.data.fakes.usecases.simplelogin.FakeObserveSimpleLoginSyncStatus
import proton.android.pass.domain.simplelogin.SimpleLoginSyncStatus
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.Autofill
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.Invite
import proton.android.pass.features.home.onboardingtips.OnBoardingTipPage.NotificationPermission
import proton.android.pass.features.home.onboardingtips.OnBoardingTipsUiState
import proton.android.pass.features.home.onboardingtips.OnBoardingTipsViewModel
import proton.android.pass.notifications.fakes.TestNotificationManager
import proton.android.pass.preferences.HasDismissedAutofillBanner
import proton.android.pass.preferences.HasDismissedNotificationBanner
import proton.android.pass.preferences.HasDismissedSLSyncBanner
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
    private lateinit var observeInvites: TestObserveInvites
    private lateinit var notificationManager: TestNotificationManager
    private lateinit var appConfig: TestAppConfig
    private lateinit var observeSimpleLoginSyncStatus: FakeObserveSimpleLoginSyncStatus

    @Before
    fun setUp() {
        preferenceRepository = TestPreferenceRepository()
        autofillManager = TestAutofillManager()
        observeInvites = TestObserveInvites()
        notificationManager = TestNotificationManager()
        appConfig = TestAppConfig()
        observeSimpleLoginSyncStatus = FakeObserveSimpleLoginSyncStatus()
        viewModel = OnBoardingTipsViewModel(
            autofillManager = autofillManager,
            preferencesRepository = preferenceRepository,
            observeInvites = observeInvites,
            notificationManager = notificationManager,
            appConfig = appConfig,
            observeSimpleLoginSyncStatus = observeSimpleLoginSyncStatus
        )
    }

    @Test
    fun `Should not show banner if autofill is unsupported`() = runTest {
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should not show banner if autofill is enabled by our service`() = runTest {
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should show banner if autofill is enabled by other service`() = runTest {
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOtherService))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(Autofill.some()))
        }
    }

    @Test
    fun `Should show banner if autofill is disabled `() = runTest {
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(Autofill.some()))
        }
    }

    @Test
    fun `Should not show banner if autofill banner has been dismised`() = runTest {
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.Dismissed)
        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState())
        }
    }

    @Test
    fun `Should display invite banner regardless of the state of the other conditions`() = runTest {
        val pendingInvite = TestPendingInvite.Vault.create()
        setupSyncStatus()
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        preferenceRepository.setHasDismissedAutofillBanner(HasDismissedAutofillBanner.NotDismissed)
        observeInvites.emitInvites(listOf(pendingInvite))

        viewModel.stateFlow.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingTipsUiState(Invite(pendingInvite).some()))
        }
    }

    @Test
    fun `Should display notification permission banner if not notification permission and banner not dismissed`() =
        runTest {
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
        setupSyncStatus()
        preferenceRepository.setHasDismissedSLSyncBanner(HasDismissedSLSyncBanner.Dismissed)

        viewModel.stateFlow.test {
            assertThat(awaitItem().tipToShow).isNotEqualTo(NotificationPermission)
        }
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
