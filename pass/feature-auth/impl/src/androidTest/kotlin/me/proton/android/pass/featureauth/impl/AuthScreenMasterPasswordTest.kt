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

package me.proton.android.pass.featureauth.impl

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.biometry.BiometryAuthError
import proton.android.pass.biometry.BiometryResult
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.biometry.TestBiometryManager
import proton.android.pass.common.fakes.TestAppDispatchers
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.data.fakes.usecases.TestCheckMasterPassword
import proton.android.pass.data.fakes.usecases.TestObservePrimaryUserEmail
import proton.android.pass.featureauth.impl.AuthNavigation
import proton.android.pass.featureauth.impl.AuthScreen
import proton.android.pass.featureauth.impl.AuthViewModel
import proton.android.pass.featureauth.impl.R
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.TestConstants.BULLET
import proton.android.pass.test.waitUntilExists
import proton.android.pass.test.writeTextAndWait
import javax.inject.Inject
import me.proton.core.presentation.R as CoreR

@HiltAndroidTest
class AuthScreenMasterPasswordTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var biometryManager: TestBiometryManager

    @Inject
    lateinit var checkMasterPassword: TestCheckMasterPassword

    @Inject
    lateinit var appDispatchers: TestAppDispatchers

    @Before
    fun setup() {
        hiltRule.inject()

        biometryManager.setBiometryStatus(BiometryStatus.CanAuthenticate)
        biometryManager.emitResult(BiometryResult.Error(BiometryAuthError.Canceled))
        runBlocking {
            userPreferencesRepository.setBiometricLockState(BiometricLockState.Enabled)
        }
    }

    @Test
    fun displaysUserEmail() {
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AuthScreen(
                        navigation = {},
                        canLogout = true
                    )
                }
            }

            onNodeWithText(TestObservePrimaryUserEmail.DEFAULT_EMAIL, substring = true).assertExists()
        }
    }

    @Test
    fun displaysLogoutButtonIfCanLogout() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AuthScreen(
                        navigation = {
                            if (it == AuthNavigation.SignOut) {
                                checker.call()
                            }
                        },
                        canLogout = true
                    )
                }
            }

            val contentDescription = activity.getString(CoreR.string.presentation_menu_item_title_sign_out)
            onNodeWithContentDescription(contentDescription).assertExists().performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun doesNotDisplayLogoutButtonIfCanLogout() {
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AuthScreen(
                        navigation = {},
                        canLogout = false
                    )
                }
            }

            val contentDescription = activity.getString(CoreR.string.presentation_menu_item_title_sign_out)
            onNodeWithContentDescription(contentDescription).assertDoesNotExist()
        }
    }

    @Test
    fun rightPasswordCallsSuccess() {
        val checker = CallChecker<Unit>()
        val password = "password"
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AuthScreen(
                        navigation = {
                            if (it == AuthNavigation.Success) {
                                checker.call()
                            }
                        },
                        canLogout = false
                    )
                }
            }

            val label = activity.getString(R.string.auth_master_password_label)
            onNodeWithText(label).performClick()

            writeTextAndWait(hasText(label), password, BULLET.repeat(password.length))

            val unlockButtonText = activity.getString(R.string.auth_unlock_button)
            onNodeWithText(unlockButtonText).performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun wrongPasswordShowsError() {
        val password = "password"
        checkMasterPassword.setResult(false)
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AuthScreen(
                        navigation = {},
                        canLogout = false
                    )
                }
            }

            val label = activity.getString(R.string.auth_master_password_label)
            onNodeWithText(label).performClick()

            writeTextAndWait(hasText(label), password, BULLET.repeat(password.length))

            val unlockButtonText = activity.getString(R.string.auth_unlock_button)
            onNodeWithText(unlockButtonText).performClick()

            val errorText = activity.resources.getQuantityString(R.plurals.auth_error_wrong_password, 4, 4)
            waitUntilExists(hasText(errorText), timeoutMillis = 2500)
        }
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun tooManyWrongPasswordsForceLogout() {
        val password = "password"
        checkMasterPassword.setResult(false)

        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AuthScreen(
                        navigation = {
                            if (it == AuthNavigation.ForceSignOut) {
                                checker.call()
                            }
                        },
                        canLogout = false
                    )
                }
            }

            val label = activity.getString(R.string.auth_master_password_label)
            onNodeWithText(label).performClick()

            writeTextAndWait(hasText(label), password, BULLET.repeat(password.length))

            val unlockButtonText = activity.getString(R.string.auth_unlock_button)

            val maxWrongAttempts = AuthViewModel.MAX_WRONG_PASSWORD_ATTEMPTS
            repeat(maxWrongAttempts - 1) {
                onNodeWithText(unlockButtonText).performClick()
                if (it > 0) {
                    val expectedNumber = maxWrongAttempts - it
                    val errorText = activity.resources.getQuantityString(
                        R.plurals.auth_error_wrong_password,
                        expectedNumber,
                        expectedNumber
                    )
                    waitUntilDoesNotExist(hasText(errorText))
                }
                val expectedNumber = maxWrongAttempts - it - 1
                val errorText = activity.resources.getQuantityString(
                    R.plurals.auth_error_wrong_password,
                    expectedNumber,
                    expectedNumber
                )

                appDispatchers.advanceTimeBy(2500L)

                waitUntilExists(hasText(errorText))
            }

            onNodeWithText(unlockButtonText).performClick()

            appDispatchers.advanceTimeBy(2500L)

            waitUntil { checker.isCalled }
        }
    }

}
