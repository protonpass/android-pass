package proton.android.pass.featuresettings.impl

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity

@HiltAndroidTest
class ThemeSelectionBottomSheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Test
    fun onThemeSelectShouldDismissBottomsheet() {
        val checker = CallChecker()
        composeTestRule.setContent {
            ThemeSelectionBottomSheet(
                dismissBottomSheet = { checker.call() }
            )
        }
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.settings_appearance_preference_subtitle_dark))
            .performClick()
        composeTestRule.waitUntil { checker.isCalled }
    }
}
