package me.proton.android.pass.featureauth.impl

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.TestBiometryManager
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.test.HiltComponentActivity
import javax.inject.Inject

@HiltAndroidTest
class AuthScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var biometryManager: TestBiometryManager

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun onBiometricLockDisabledAuthSuccessCalled() {
        var isCalled = false
        runBlocking {
            userPreferencesRepository.setBiometricLockState(BiometricLockState.Disabled)
        }
        composeTestRule.setContent {
            AuthScreen(
                onAuthSuccessful = { isCalled = true },
                onAuthFailed = {},
                onAuthDismissed = {}
            )
        }
        composeTestRule.waitUntil { isCalled }
    }

    @Test
    fun onBiometricLockEnabledAndBiometricResultSuccessAuthSuccessCalled() {
        var isCalled = false
        runBlocking {
            userPreferencesRepository.setBiometricLockState(BiometricLockState.Enabled)
            biometryManager.emitResult(BiometryResult.Success)
        }
        composeTestRule.setContent {
            AuthScreen(
                onAuthSuccessful = { isCalled = true },
                onAuthFailed = {},
                onAuthDismissed = {}
            )
        }
        composeTestRule.waitUntil { isCalled }
    }

    @Test
    fun onBiometricLockEnabledAndBiometricResultErrorAuthFailedCalled() {
        var isCalled = false
        runBlocking {
            userPreferencesRepository.setBiometricLockState(BiometricLockState.Enabled)
            biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.NoBiometrics))
        }
        composeTestRule.setContent {
            AuthScreen(
                onAuthSuccessful = { },
                onAuthFailed = { isCalled = true },
                onAuthDismissed = {}
            )
        }
        composeTestRule.waitUntil { isCalled }
    }

    @Test
    fun onBiometricLockEnabledAndBiometricResultCanceledAuthDismissedCalled() {
        var isCalled = false
        runBlocking {
            userPreferencesRepository.setBiometricLockState(BiometricLockState.Enabled)
            biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.Canceled))
        }
        composeTestRule.setContent {
            AuthScreen(
                onAuthSuccessful = { },
                onAuthFailed = { },
                onAuthDismissed = { isCalled = true }
            )
        }
        composeTestRule.waitUntil { isCalled }
    }
}
