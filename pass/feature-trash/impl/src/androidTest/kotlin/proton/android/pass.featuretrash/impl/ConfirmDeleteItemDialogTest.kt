package proton.android.pass.featuretrash.impl

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import me.proton.core.presentation.R as CoreR

class ConfirmDeleteItemDialogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun dialogOnConfirmIsCalled() {
        var isCalled = false
        composeTestRule.setContent {
            ConfirmDeleteItemDialog(
                show = true,
                isLoading = false,
                onDismiss = {},
                onConfirm = {
                    isCalled = true
                }
            )
        }
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(CoreR.string.presentation_alert_ok))
            .performClick()
        assert(isCalled)
    }
}
