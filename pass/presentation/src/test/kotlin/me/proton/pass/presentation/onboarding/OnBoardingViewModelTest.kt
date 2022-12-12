package me.proton.pass.presentation.onboarding

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.android.pass.autofill.api.AutofillStatus
import me.proton.android.pass.autofill.api.AutofillSupportedStatus
import me.proton.android.pass.autofill.fakes.TestAutofillManager
import me.proton.android.pass.biometry.BiometryResult
import me.proton.android.pass.biometry.BiometryStatus
import me.proton.android.pass.biometry.ContextHolder
import me.proton.android.pass.biometry.TestBiometryManager
import me.proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.HasAuthenticated
import me.proton.android.pass.preferences.HasCompletedOnBoarding
import me.proton.android.pass.preferences.TestPreferenceRepository
import me.proton.pass.common.api.None
import me.proton.pass.presentation.onboarding.OnBoardingPageName.Autofill
import me.proton.pass.presentation.onboarding.OnBoardingPageName.Fingerprint
import me.proton.pass.presentation.onboarding.OnBoardingPageName.Last
import me.proton.pass.test.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class OnBoardingViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: OnBoardingViewModel
    private lateinit var snackbarMessageRepository: TestSnackbarMessageRepository
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var biometryManager: TestBiometryManager
    private lateinit var autofillManager: TestAutofillManager

    @Before
    fun setUp() {
        snackbarMessageRepository = TestSnackbarMessageRepository()
        preferenceRepository = TestPreferenceRepository()
        biometryManager = TestBiometryManager()
        autofillManager = TestAutofillManager()
    }

    @Test
    fun `sends correct initial state`() = runTest {
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            assertThat(awaitItem()).isEqualTo(OnBoardingUiState.Initial)
        }
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
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            autofillManager.emitStatus(AutofillSupportedStatus.Unsupported)
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(enabledPages = setOf(Fingerprint, Last))
            )
        }
    }

    @Test
    fun `given already enabled autofill should show 1 screen`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
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
                viewModel.onMainButtonClick(Autofill, ContextHolder(None))
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
                viewModel.onMainButtonClick(Autofill, ContextHolder(None))
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
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.Disabled))
            assertThat(awaitItem()).isEqualTo(
                OnBoardingUiState.Initial.copy(enabledPages = setOf(Autofill, Last))
            )
        }
    }

    @Test
    fun `given a click on enable fingerprint should select last page`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        autofillManager.emitStatus(AutofillSupportedStatus.Supported(AutofillStatus.EnabledByOurService))
        viewModel = createViewModel()
        viewModel.onBoardingUiState.test {
            skipItems(1)
            biometryManager.emitResult(BiometryResult.Success)
            preferenceRepository.setHasAuthenticated(HasAuthenticated.Authenticated)
            preferenceRepository.setBiometricLockState(BiometricLockState.Enabled)
            viewModel.onMainButtonClick(Fingerprint, ContextHolder(None))
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
            viewModel.onMainButtonClick(Last, ContextHolder(None))
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
