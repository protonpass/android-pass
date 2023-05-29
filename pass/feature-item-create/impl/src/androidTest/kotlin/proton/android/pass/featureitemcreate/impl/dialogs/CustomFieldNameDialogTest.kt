package proton.android.pass.featureitemcreate.impl.dialogs

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.data.api.repositories.DRAFT_CUSTOM_FIELD_KEY
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.bottomsheets.customfield.CustomFieldType
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.pass.domain.CustomFieldContent
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import me.proton.core.presentation.compose.R as CoreR

@HiltAndroidTest
class CustomFieldNameDialogTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var savedStateProvider: SavedStateHandleProvider

    @Inject
    lateinit var draftRepository: TestDraftRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }
    @Test
    fun testAddTextField() {
        performTest(CustomFieldType.Text, CustomFieldContent.Text(label = LABEL, value = ""))
    }

    @Test
    fun testAddTotpField() {
        performTest(CustomFieldType.Totp, CustomFieldContent.Totp(label = LABEL, value = ""))
    }

    @Test
    fun testAddHiddenField() {
        performTest(CustomFieldType.Hidden, CustomFieldContent.Hidden(label = LABEL, value = ""))
    }

    private fun performTest(type: CustomFieldType, expected: CustomFieldContent) {
        savedStateProvider.get()[CustomFieldTypeNavArgId.key] = type.name

        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    CustomFieldNameDialog(
                        onNavigate = {
                            if (it == CustomFieldNameNavigation.Close) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val placeholder = activity.getString(R.string.custom_field_dialog_placeholder)
            val confirmText = activity.getString(CoreR.string.presentation_alert_ok)
            onNodeWithText(placeholder).performTextInput(LABEL)
            onNodeWithText(confirmText).performClick()

            waitUntil { checker.isCalled }
        }

        runBlocking {
            val customField = draftRepository
                .get<CustomFieldContent>(DRAFT_CUSTOM_FIELD_KEY)
                .first()

            assertNotNull(customField.value())
            assertEquals(expected, customField.value())
        }
    }

    companion object {
        private const val LABEL ="Custom field name"
    }

}
