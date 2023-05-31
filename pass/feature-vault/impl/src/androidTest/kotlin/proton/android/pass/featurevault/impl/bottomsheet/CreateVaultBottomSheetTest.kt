package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.data.fakes.usecases.TestCreateVault
import proton.android.pass.feature.vault.impl.R
import proton.android.pass.featurevault.impl.VaultNavigation
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.waitUntilExists
import javax.inject.Inject
import kotlin.test.assertEquals
import proton.android.pass.composecomponents.impl.R as CompR

@HiltAndroidTest
class CreateVaultBottomSheetTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var createVault: TestCreateVault

    private val submitButtonMatcher: SemanticsMatcher
        get() = hasText(composeTestRule.activity.getString(R.string.bottomsheet_create_vault_button))

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun canCreateVault() {
        createVault.setResult(Result.success(TestShare.create()))
        val vaultName = "Some vault with trailing space "

        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    CreateVaultBottomSheet(
                        onNavigate = {
                            if (it == VaultNavigation.Close) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val placeholder = activity.getString(CompR.string.field_title_title)

            waitUntilExists(hasText(placeholder))
            onNodeWithText(placeholder).performTextInput(vaultName)

            onNode(submitButtonMatcher).performClick()
        }

        val memory = createVault.memory()
        assertEquals(1, memory.size)

        val payload = memory.first().vault
        val payloadVaultName = TestEncryptionContext.decrypt(payload.name)
        assertEquals("Some vault with trailing space", payloadVaultName)

    }

    @Test
    fun cannotCreateVaultWithEmptyContents() {
        composeTestRule.apply {
            setContent {
                PassTheme {
                    CreateVaultBottomSheet(
                        onNavigate = {}
                    )
                }
            }

            val placeholder = activity.getString(CompR.string.field_title_title)

            waitUntilExists(hasText(placeholder))
            onNodeWithText(placeholder).performTextInput("Some vault")

            onNode(submitButtonMatcher).performClick()
        }
    }
}
