package proton.android.pass.autofill

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.autofill.ui.autofill.AutofillAppUiState
import proton.android.pass.autofill.ui.autofill.AutofillAppViewModel
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.TestBiometryManager
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.notifications.fakes.TestNotificationManager
import proton.android.pass.notifications.fakes.TestSnackbarMessage
import proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.totp.api.TotpManager
import proton.android.pass.totp.fakes.TestTotpManager

class AutofillAppViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: AutofillAppViewModel
    private lateinit var preferenceRepository: TestPreferenceRepository
    private lateinit var biometryManager: TestBiometryManager
    private lateinit var snackbarMessageRepository: TestSnackbarMessageRepository
    private lateinit var clipboardManager: ClipboardManager
    private lateinit var totpManager: TotpManager
    private lateinit var notificationManager: NotificationManager

    @Before
    fun setUp() {
        preferenceRepository = TestPreferenceRepository()
        biometryManager = TestBiometryManager()
        snackbarMessageRepository = TestSnackbarMessageRepository()
        clipboardManager = TestClipboardManager()
        totpManager = TestTotpManager()
        notificationManager = TestNotificationManager()
        viewModel = AutofillAppViewModel(
            preferenceRepository,
            biometryManager,
            TestEncryptionContextProvider(),
            clipboardManager,
            totpManager,
            notificationManager,
            snackbarMessageRepository
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
        preferenceRepository.setThemePreference(ThemePreference.System)
        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        preferenceRepository.setBiometricLockState(BiometricLockState.Enabled)
        preferenceRepository.setCopyTotpToClipboardEnabled(CopyTotpToClipboard.Enabled)
        snackbarMessageRepository.emitSnackbarMessage(TestSnackbarMessage())

        viewModel.state.test {
            assertThat(awaitItem().isFingerprintRequired).isTrue()
        }
    }
}
