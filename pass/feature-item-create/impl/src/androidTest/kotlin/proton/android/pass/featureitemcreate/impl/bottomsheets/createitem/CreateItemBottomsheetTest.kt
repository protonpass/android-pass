package proton.android.pass.featureitemcreate.impl.bottomsheets.createitem

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
class CreateItemBottomsheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()
    
    @Test
    fun testCreateLoginFullMode() {
        performTest(
            mode = CreateItemBottomSheetMode.Full,
            text = R.string.item_type_login_description,
        ) { navigation, checker ->
            if (navigation is CreateItemBottomsheetNavigation.CreateLogin) {
                checker.call(Unit)
            }
        }
    }

    @Test
    fun testCreateLoginAutofillMode() {
        performTest(
            mode = CreateItemBottomSheetMode.Autofill,
            text = R.string.item_type_login_description,
        ) { navigation, checker ->
            if (navigation is CreateItemBottomsheetNavigation.CreateLogin) {
                checker.call(Unit)
            }
        }
    }

    @Test
    fun testCreateAliasFullMode() {
        performTest(
            mode = CreateItemBottomSheetMode.Full,
            text = R.string.item_type_alias_description,
        ) { navigation, checker ->
            if (navigation is CreateItemBottomsheetNavigation.CreateAlias) {
                checker.call(Unit)
            }
        }
    }

    @Test
    fun testCreateAliasAutofillMode() {
        performTest(
            mode = CreateItemBottomSheetMode.Autofill,
            text = R.string.item_type_alias_description,
        ) { navigation, checker ->
            if (navigation is CreateItemBottomsheetNavigation.CreateAlias) {
                checker.call(Unit)
            }
        }
    }

    @Test
    fun testCreateNote() {
        performTest(
            mode = CreateItemBottomSheetMode.Full,
            text = R.string.item_type_note_description,
        ) { navigation, checker ->
            if (navigation is CreateItemBottomsheetNavigation.CreateNote) {
                checker.call(Unit)
            }
        }
    }

    @Test
    fun testCreatePassword() {
        performTest(
            mode = CreateItemBottomSheetMode.Full,
            text = R.string.item_type_password_description,
        ) { navigation, checker ->
            if (navigation is CreateItemBottomsheetNavigation.CreatePassword) {
                checker.call(Unit)
            }
        }
    }

    private fun performTest(
        mode: CreateItemBottomSheetMode,
        @StringRes text: Int,
        callback: (CreateItemBottomsheetNavigation, CallChecker<Unit>) -> Unit
    ) {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    CreateItemBottomSheet(
                        mode = mode,
                        onNavigate =  { callback(it, checker) }
                    )
                }
            }

            onNodeWithText(activity.getString(text)).performClick()
            waitUntil { checker.isCalled }
        }
    }

}
