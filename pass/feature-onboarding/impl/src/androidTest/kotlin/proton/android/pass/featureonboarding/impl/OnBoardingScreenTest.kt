package proton.android.pass.featureonboarding.impl

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.IntSize
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import proton.android.pass.test.HiltComponentActivity

@HiltAndroidTest
class OnBoardingScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    private val notNowButtonMatcher by lazy {
        hasText(composeTestRule.activity.resources.getString(R.string.on_boarding_skip))
    }

    private val fingerprintMatcher by lazy {
        hasText(composeTestRule.activity.resources.getString(R.string.on_boarding_fingerprint_button))
    }

    private val startMatcher by lazy {
        hasText(composeTestRule.activity.resources.getString(R.string.on_boarding_last_page_button))
    }

    @Test
    fun onBoardingCanBeCompleted() {
        var isCalled = false

        composeTestRule.setContent {
            OnBoardingScreen(
                onBoardingShown = { isCalled = true }
            )
        }

        composeTestRule
            .onAllNodes(notNowButtonMatcher)[0]
            .performClick()
        composeTestRule.waitUntil {
            composeTestRule
                .onNode(fingerprintMatcher)
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .size != IntSize.Zero
        }
        composeTestRule
            .onAllNodes(notNowButtonMatcher)[0]
            .performClick()
        composeTestRule.waitUntil {
            composeTestRule
                .onNode(startMatcher)
                .assertIsDisplayed()
                .fetchSemanticsNode()
                .size != IntSize.Zero
        }
        composeTestRule.onNode(startMatcher).performClick()
        composeTestRule.waitUntil { isCalled }
    }
}
