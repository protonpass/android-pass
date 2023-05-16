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
}
