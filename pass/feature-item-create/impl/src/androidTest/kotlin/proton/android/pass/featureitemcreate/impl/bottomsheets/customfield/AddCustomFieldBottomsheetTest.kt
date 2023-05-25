package proton.android.pass.featureitemcreate.impl.bottomsheets.customfield

import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity

@HiltAndroidTest
class AddCustomFieldBottomsheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Test
    fun testAddTextField() {
        performTest(R.string.bottomsheet_custom_field_type_text, CustomFieldNavigation.AddText)
    }

    @Test
    fun testAddTotpField() {
        performTest(R.string.bottomsheet_custom_field_type_totp, CustomFieldNavigation.AddTotp)
    }

    @Test
    fun testAddHiddenField() {
        performTest(R.string.bottomsheet_custom_field_type_hidden, CustomFieldNavigation.AddHidden)
    }

    private fun performTest(@StringRes text: Int, navigation: CustomFieldNavigation) {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    AddCustomFieldBottomSheet(
                        onNavigate = {
                            if (it == navigation) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            onNodeWithText(activity.getString(text)).performClick()
            waitUntil { checker.isCalled }
        }
    }
}

