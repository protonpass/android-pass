/*
 * Copyright (c) 2023-2024 Proton AG
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

import android.content.Intent.ACTION_VIEW
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.rule.IntentsRule
import org.junit.Rule
import org.junit.Test
import proton.android.pass.features.profile.FeedbackBottomsheet
import proton.android.pass.features.profile.PASS_REDDIT
import proton.android.pass.features.profile.PASS_USERVOICE
import proton.android.pass.features.profile.ProfileNavigation
import proton.android.pass.features.profile.R
import proton.android.pass.test.CallChecker

class FeedbackBottomsheetTest {

    @get:Rule(order = 0)
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule(order = 1)
    val intentsRule = IntentsRule()

    @Test
    fun feedbackSendEmailCalled() {
        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            FeedbackBottomsheet(
                onNavigateEvent = {
                    when (it) {
                        is ProfileNavigation.Report -> checker.call()
                        else -> {}
                    }
                }
            )
        }
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.feedback_option_mail))
            .performClick()
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun feedbackOpenRedditCalled() {
        composeTestRule.setContent {
            FeedbackBottomsheet { }
        }
        composeTestRule
            .onNodeWithText(composeTestRule.activity.resources.getString(R.string.feedback_option_reddit))
            .performClick()
        intended(hasAction(ACTION_VIEW))
        intended(hasData(PASS_REDDIT))
    }

    @Test
    fun feedbackUserVoiceCalled() {
        composeTestRule.apply {
            setContent {
                FeedbackBottomsheet { }
            }

            val text = activity.getString(R.string.feedback_option_vote_new_features)
            onNodeWithText(text).performClick()
            intended(hasAction(ACTION_VIEW))
            intended(hasData(PASS_USERVOICE))
        }
    }
}
