/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.extrapassword

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.data.api.errors.TooManyExtraPasswordAttemptsException
import proton.android.pass.data.fakes.usecases.accesskey.FakeAuthWithExtraPassword
import proton.android.pass.features.extrapassword.auth.ui.EnterExtraPasswordScreen
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import javax.inject.Inject
import kotlin.test.assertEquals

@HiltAndroidTest
class EnterExtraPasswordNavItemScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var authWithExtraPassword: FakeAuthWithExtraPassword

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun canEnterExtraPassword() {
        authWithExtraPassword.setResult(Result.success(Unit))

        val checker = CallChecker<Unit>()
        runTest(
            onSuccess = { checker.call() },
            onLogout = {},
            checker = checker
        )
    }

    @Test
    fun emitLogoutOnTooManyWrongAttempts() {
        authWithExtraPassword.setResult(Result.failure(TooManyExtraPasswordAttemptsException()))

        val checker = CallChecker<UserId>()
        runTest(
            onSuccess = { },
            onLogout = { checker.call(it) },
            checker = checker
        )

        assertEquals(USER_ID, checker.memory)
    }

    private fun <T> runTest(
        onSuccess: () -> Unit,
        onLogout: (UserId) -> Unit,
        checker: CallChecker<T>
    ) {
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    EnterExtraPasswordScreen(
                        userId = USER_ID,
                        onSuccess = onSuccess,
                        onLogout = onLogout
                    )
                }
            }

            onNodeWithText(activity.getString(R.string.extra_password_label))
                .performClick()
                .performTextInput("password")

            onNodeWithText(activity.getString(me.proton.core.presentation.compose.R.string.presentation_alert_submit))
                .performClick()

            waitUntil { checker.isCalled }
        }
    }

    companion object {
        private val USER_ID = UserId("TEST_USER_ID")
    }
}
