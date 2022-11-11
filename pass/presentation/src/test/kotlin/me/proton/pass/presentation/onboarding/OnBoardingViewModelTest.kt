package me.proton.pass.presentation.onboarding

import me.proton.android.pass.autofill.fakes.TestAutofillManager
import me.proton.android.pass.biometry.TestBiometryManager
import me.proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import me.proton.android.pass.preferences.TestPreferenceRepository
import me.proton.pass.test.MainDispatcherRule
import org.junit.Before
import org.junit.Rule

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
        viewModel = OnBoardingViewModel(
            autofillManager,
            biometryManager,
            preferenceRepository,
            snackbarMessageRepository
        )
    }
}
