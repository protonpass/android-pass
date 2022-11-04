package me.proton.pass.autofill

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.android.pass.biometry.BiometryStatus
import me.proton.android.pass.biometry.TestBiometryManager
import me.proton.android.pass.preferences.BiometricLockState
import me.proton.android.pass.preferences.TestPreferenceRepository
import me.proton.android.pass.preferences.ThemePreference
import me.proton.pass.autofill.ui.autofill.AutofillAppUiState
import me.proton.pass.autofill.ui.autofill.AutofillAppViewModel
import me.proton.pass.test.MainDispatcherRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AutofillAppViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AutofillAppViewModel
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var biometryManager: TestBiometryManager

    @Before
    fun setUp() {
        preferenceRepository = TestPreferenceRepository()
        preferenceRepository.setThemePreference(ThemePreference.System)
        preferenceRepository.setBiometricLockState(BiometricLockState.Disabled)

        biometryManager = TestBiometryManager()
        viewModel = AutofillAppViewModel(
            preferenceRepository,
            biometryManager
        )
    }

    @Test
    fun `should emit initial state`() = runTest {
        viewModel.state.test {
            assertThat(awaitItem()).isEqualTo(AutofillAppUiState.Initial)
        }
    }

    @Test
    fun `if biometry is not available fingerprint is not required`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.NotAvailable)
        viewModel.state.test {
            assertThat(awaitItem().isFingerprintRequired).isFalse()
        }
    }

    @Test
    fun `if biometry is not enrolled fingerprint is not required`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.NotEnrolled)
        viewModel.state.test {
            assertThat(awaitItem().isFingerprintRequired).isFalse()
        }
    }

    @Test
    fun `if biometry is available preference is returned (Disabled)`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        preferenceRepository.setBiometricLockState(BiometricLockState.Disabled)
        viewModel.state.test {
            assertThat(awaitItem().isFingerprintRequired).isFalse()
        }
    }

    @Test
    fun `if biometry is available preference is returned (Enabled)`() = runTest {
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        preferenceRepository.setBiometricLockState(BiometricLockState.Enabled)

        viewModel.state.test {
            assertThat(awaitItem().isFingerprintRequired).isTrue()
        }
    }
}
