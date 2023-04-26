package proton.android.pass.featureaccount.impl

import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.rule.IntentsRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.composecomponents.impl.R as CompR

@HiltAndroidTest
class AccountScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @get:Rule(order = 2)
    val intentsRule = IntentsRule()

    @Test
    fun accountScreenOnSignOutIsCalled() {
        var isCalled = false
        composeTestRule.setContent {
            AccountScreen(
                onSignOutClick = { isCalled = true },
                onUpClick = {},
                onCurrentSubscriptionClick = {}
            )
        }
        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(
                    R.string.account_sign_out_icon_content_description
                )
            )
            .performClick()
        assert(isCalled)
    }

    @Test
    fun accountScreenOnBackIsCalled() {
        var isCalled = false
        composeTestRule.setContent {
            AccountScreen(
                onSignOutClick = {},
                onUpClick = { isCalled = true },
                onCurrentSubscriptionClick = {}
            )
        }
        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(CompR.string.navigate_back_icon_content_description)
            )
            .performClick()
        assert(isCalled)
    }

    @Test
    fun accountScreenOnDeleteOpensWebsite() {
        composeTestRule.setContent {
            AccountScreen(
                onSignOutClick = {},
                onUpClick = {},
                onCurrentSubscriptionClick = {}
            )
        }
        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(R.string.account_delete_account_icon_content_description)
            )
            .performClick()
        intended(hasAction(Intent.ACTION_VIEW))
        intended(hasData("https://account.proton.me/u/0/pass/account-password"))
    }


    @Test
    fun accountScreenOnManageSubscriptionOpensWebsite() {
        composeTestRule.setContent {
            AccountScreen(
                onSignOutClick = {},
                onUpClick = {},
                onCurrentSubscriptionClick = {}
            )
        }
        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(R.string.manage_subscription_icon_content_description)
            )
            .performClick()
        intended(hasAction(Intent.ACTION_VIEW))
        intended(hasData("https://account.proton.me/u/0/pass/dashboard"))
    }
}
