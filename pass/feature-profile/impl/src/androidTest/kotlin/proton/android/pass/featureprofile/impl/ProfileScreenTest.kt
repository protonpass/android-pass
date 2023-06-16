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

package proton.android.pass.featureprofile.impl

import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.rule.IntentsRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity

@HiltAndroidTest
class ProfileScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @get:Rule(order = 2)
    val intentsRule = IntentsRule()

    @Test
    fun onAccountClickCalled() {
        val checker = CallChecker<Unit>()

        composeTestRule.setContent {
            PassTheme {
                ProfileScreen(
                    onNavigateEvent = {
                        when (it) {
                            is ProfileNavigation.Account -> checker.call()
                            else -> {}
                        }
                    },
                )
            }
        }
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.profile_option_settings))
            .performScrollTo()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.profile_option_account))
            .performClick()
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun onSettingsClickCalled() {
        val checker = CallChecker<Unit>()

        composeTestRule.setContent {
            PassTheme {
                ProfileScreen(
                    onNavigateEvent = {
                        when (it) {
                            is ProfileNavigation.Settings -> checker.call()
                            else -> {}
                        }
                    },
                )
            }
        }
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.profile_option_feedback))
            .performScrollTo()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.profile_option_settings))
            .performClick()
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun onFeedbackClickCalled() {
        val checker = CallChecker<Unit>()

        composeTestRule.setContent {
            PassTheme {
                ProfileScreen(
                    onNavigateEvent = {
                        when (it) {
                            is ProfileNavigation.Feedback -> checker.call()
                            else -> {}
                        }
                    },
                )
            }
        }
        composeTestRule.onNodeWithText("0.0.0")
            .performScrollTo()
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.profile_option_feedback))
            .performClick()
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun onRateAppClickCalled() {
        composeTestRule.setContent {
            PassTheme {
                ProfileScreen(
                    onNavigateEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("0.0.0")
            .performScrollTo()

        composeTestRule.onNodeWithText(composeTestRule.activity.resources.getString(R.string.profile_option_rating))
            .performClick()

        intended(hasAction(Intent.ACTION_VIEW))
        intended(hasData(PASS_STORE))
    }


    @Test
    fun onImportExportClickCalled() {
        composeTestRule.setContent {
            PassTheme {
                ProfileScreen(
                    onNavigateEvent = {},
                )
            }
        }
        composeTestRule.onNodeWithText("0.0.0")
            .performScrollTo()

        composeTestRule.onNodeWithText(
            composeTestRule.activity.resources.getString(R.string.profile_option_import_export)
        )
            .performClick()

        intended(hasAction(Intent.ACTION_VIEW))
        intended(hasData(PASS_IMPORT))
    }
}
