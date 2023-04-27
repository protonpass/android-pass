package me.proton.android.pass.featureauth.impl

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.TestBiometryManager
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.test.CallChecker
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
        val checker = CallChecker<Unit>()
        runBlocking {
            userPreferencesRepository.setBiometricLockState(BiometricLockState.Disabled)
        }
        composeTestRule.setContent {
            PassTheme {
                AuthScreen(
                    navigation = {
                        when (it) {
                            AuthNavigation.Success -> { checker.call() }
                            else -> {}
                        }
                    }
                )
            }
        }
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun onBiometricLockEnabledAndBiometricResultSuccessAuthSuccessCalled() {
        val checker = CallChecker<Unit>()
        runBlocking {
            userPreferencesRepository.setBiometricLockState(BiometricLockState.Enabled)
            biometryManager.emitResult(BiometryResult.Success)
        }
        composeTestRule.setContent {
            PassTheme {
                AuthScreen(
                    navigation = {
                        when (it) {
                            AuthNavigation.Success -> { checker.call() }
                            else -> {}
                        }
                    }
                )
            }
        }
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun onBiometricLockEnabledAndBiometricResultErrorAuthFailedCalled() {
        val checker = CallChecker<Unit>()
        runBlocking {
            userPreferencesRepository.setBiometricLockState(BiometricLockState.Enabled)
            biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.NoBiometrics))
        }
        composeTestRule.setContent {
            PassTheme {
                AuthScreen(
                    navigation = {
                        when (it) {
                            AuthNavigation.Failed -> {
                                checker.call()
                            }

                            else -> {}
                        }
                    }
                )
            }
        }
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun onBiometricLockEnabledAndBiometricResultCanceledAuthDismissedCalled() {
        val checker = CallChecker<Unit>()
        runBlocking {
            userPreferencesRepository.setBiometricLockState(BiometricLockState.Enabled)
            biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.Canceled))
        }
        composeTestRule.setContent {
            PassTheme {
                AuthScreen(
                    navigation = {
                        when (it) {
                            AuthNavigation.Dismissed -> { checker.call() }

                            else -> {}
                        }
                    }
                )
            }
        }
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object AuthScreenModule {

        @Provides
        fun provideClock(): Clock = Clock.System
    }
}
