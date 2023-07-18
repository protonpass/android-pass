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

package proton.android.pass.featureonboarding.impl

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.api.AutofillStatus
import proton.android.pass.autofill.api.AutofillSupportedStatus
import proton.android.pass.autofill.fakes.TestAutofillManager
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.biometry.TestBiometryManager
import proton.android.pass.common.api.None
import proton.android.pass.featureonboarding.impl.OnBoardingPageName.Autofill
import proton.android.pass.featureonboarding.impl.OnBoardingPageName.Fingerprint
import proton.android.pass.featureonboarding.impl.OnBoardingPageName.Last
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.AppLockState
import proton.android.pass.preferences.HasAuthenticated
import proton.android.pass.preferences.HasCompletedOnBoarding
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.test.MainDispatcherRule

class OnBoardingViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: OnBoardingViewModel
    private lateinit var snackbarMessageRepository: TestSnackbarDispatcher
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var biometryManager: TestBiometryManager
    private lateinit var autofillManager: TestAutofillManager

    @Before
    fun setUp() {
        snackbarMessageRepository = TestSnackbarDispatcher()
        preferenceRepository = TestPreferenceRepository()
        biometryManager = TestBiometryManager()
        autofillManager = TestAutofillManager()
    }

    @Test
    fun `given no supported features should show last page`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            preferenceRepository.setHasCompletedOnBoarding(HasCompletedOnBoarding.Completed)
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    enabledPages = setOf(
                        Last
                    )
                )
            )
        }
    }

    @Test
    fun `given unsupported autofill should show 1 screen`() = runTest {
        autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(enabledPages = setOf(Fingerprint, Last))
            )
        }
    }

    @Test
    fun `given already enabled autofill should show 1 screen`() = runTest {
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(enabledPages = setOf(Fingerprint, Last))
            )
        }
    }

    @Test
    fun `given a click on enable autofill when fingerprint is available should select next page`() =
        runTest {
            biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
            autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
            viewModel = createViewModel()
            viewModel.onBoardingUiState.test {
                skipItems(1)
                viewModel.onMainButtonClick(Autofill, ClassHolder(None))
                assertThat(awaitItem()).isEqualTo(
                    OnBoardingUiState.Initial.copy(
                        enabledPages = setOf(Autofill, Fingerprint, Last),
                        selectedPage = 1
                    )
                )
            }
        }

    @Test
    fun `given a click on enable autofill when fingerprint is not available should select next page`() =
        runTest {
            biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
            autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
            viewModel = createViewModel()
            viewModel.onBoardingUiState.test {
                skipItems(1)
                viewModel.onMainButtonClick(Autofill, ClassHolder(None))
                assertThat(awaitItem()).isEqualTo(
                    OnBoardingUiState.Initial.copy(
                        selectedPage = 1,
                        enabledPages = setOf(Autofill, Last)
                    )
                )
            }
        }

    @Test
    fun `given a click on skip autofill when fingerprint is available should select next page`() =
        runTest {
            biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
            autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
            viewModel = createViewModel()
            viewModel.onBoardingUiState.test {
                skipItems(1)
                viewModel.onSkipButtonClick(Autofill)
                assertThat(awaitItem()).isEqualTo(
                    OnBoardingUiState.Initial.copy(
                        enabledPages = setOf(Autofill, Fingerprint, Last),
                        selectedPage = 1
                    )
                )
            }
        }

    @Test
    fun `given a click on skip autofill when fingerprint is not available should select next page`() =
        runTest {
            biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
            autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
            viewModel = createViewModel()
            viewModel.onBoardingUiState.test {
                skipItems(1)
                viewModel.onSkipButtonClick(Autofill)
                assertThat(awaitItem()).isEqualTo(
                    OnBoardingUiState.Initial.copy(
                        selectedPage = 1,
                        enabledPages = setOf(Autofill, Last)
                    )
                )
            }
        }

    @Test
    fun `given unsupported biometric should show 1 screen`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(enabledPages = setOf(Autofill, Last))
            )
        }
    }

    @Test
    fun `given a click on enable fingerprint should select last page`() = runTest {
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            biometryManager.emitResult(BiometryResult.Success)
            preferenceRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
            preferenceRepository.setAppLockState(AppLockState.Enabled)
            viewModel.onMainButtonClick(Fingerprint, ClassHolder(None))
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    selectedPage = 1,
                    enabledPages = setOf(Fingerprint, Last)
                )
            )
        }
    }

    @Test
    fun `given a click on skip fingerprint should select last page`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            viewModel.onSkipButtonClick(Fingerprint)
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    selectedPage = 1,
                    enabledPages = setOf(Fingerprint, Last)
                )
            )
        }
    }

    @Test
    fun `given a click on get started in last page should complete on boarding`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            preferenceRepository.setHasCompletedOnBoarding(HasCompletedOnBoarding.Completed)
            viewModel.onMainButtonClick(Last, ClassHolder(None))
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(
                    enabledPages = setOf(Last),
                    isCompleted = true
                )
            )
        }
    }

    private fun createViewModel() =
        OnBoardingViewModel(
            autofillManager,
            biometryManager,
            preferenceRepository,
            snackbarMessageRepository
        )
}
