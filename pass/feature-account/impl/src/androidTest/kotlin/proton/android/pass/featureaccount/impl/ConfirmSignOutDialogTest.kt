package proton.android.pass.featureaccount.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import me.proton.core.presentation.R
import org.junit.Rule
import org.junit.Test
import proton.android.pass.test.CallChecker

class ConfirmSignOutDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dialogOnConfirmIsCalled() {
        val checker = CallChecker()
        composeTestRule.setContent {
            ConfirmSignOutDialog(
                show = true,
                onDismiss = {},
                onConfirm = { checker.call() }
            )
        }
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.presentation_alert_ok))
            .performClick()
        assert(checker.isCalled)
    }

    @Test
    fun dialogOnDismissIsCalled() {
        val checker = CallChecker()
        composeTestRule.setContent {
            ConfirmSignOutDialog(
                show = true,
                onDismiss = { checker.call() },
                onConfirm = {}
            )
        }
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.presentation_alert_cancel))
            .performClick()
        assert(checker.isCalled)
    }
}
