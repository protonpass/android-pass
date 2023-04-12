package proton.android.pass.featureaccount.impl

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.test.HiltComponentActivity

@HiltAndroidTest
class AccountScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun accountScreenOnSignOutIsCalled() {
        var isCalled = false
        composeTestRule.setContent {
            AccountScreen(
                onSignOutClick = { isCalled = true },
                onUpClick = {}
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
}
