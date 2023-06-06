package proton.android.pass.featuresettings.impl

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.image.fakes.TestClearIconCache
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import javax.inject.Inject
import kotlin.test.assertTrue

@HiltAndroidTest
class SettingsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var preferenceRepository: TestPreferenceRepository

    @Inject
    lateinit var clearIconCache: TestClearIconCache

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun onSelectTheme() {
        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            PassTheme {
                SettingsScreen(
                    onNavigate = {
                        if (it == SettingsNavigation.SelectTheme) {
                            checker.call()
                        }
                    }
                )
            }
        }
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.settings_appearance_preference_title))
            .performClick()
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun onClipboardClick() {
        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            PassTheme {
                SettingsScreen(
                    onNavigate = {
                        if (it is SettingsNavigation.ClipboardSettings) {
                            checker.call()
                        }
                    }
                )
            }
        }
        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.settings_option_clipboard))
            .performClick()
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun onPrimaryVaultClick() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    SettingsScreen(
                        onNavigate = {
                            if (it is SettingsNavigation.PrimaryVault) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val text = activity.getString(R.string.settings_primary_vault_vault_selector_title)
            onNodeWithText(text).performScrollTo()
            onNodeWithText(text).performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun onUpClick() {
        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            PassTheme {
                SettingsScreen(
                    onNavigate = {
                        if (it is SettingsNavigation.Close) {
                            checker.call()
                        }
                    }
                )
            }
        }
        val contentDescription = composeTestRule.activity.getString(
            proton.android.pass.composecomponents.impl.R.string.navigate_back_icon_content_description
        )

        composeTestRule
            .onNodeWithContentDescription(contentDescription)
            .performClick()
        composeTestRule.waitUntil { checker.isCalled }
    }

    @Test
    fun onUseWebIconsFromFalseToTrue() {
        testUseWebIcons(initialState = false)
    }

    @Test
    fun onUseWebIconsFromTrueToFalse() {
        testUseWebIcons(initialState = true)
        assertTrue(clearIconCache.invoked())
    }

    private fun testUseWebIcons(initialState: Boolean) {
        runBlocking {
            preferenceRepository.setUseFaviconsPreference(UseFaviconsPreference.from(initialState))

            preferenceRepository.setThemePreference(ThemePreference.System)
            preferenceRepository.setCopyTotpToClipboardEnabled(CopyTotpToClipboard.Enabled)
        }

        composeTestRule.setContent {
            PassTheme {
                SettingsScreen(
                    onNavigate = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.settings_use_favicons_preference_title))
            .performClick()

        val expected = UseFaviconsPreference.from(!initialState)
        composeTestRule.waitUntil {
            val preferenceValue = runBlocking {
                preferenceRepository.getUseFaviconsPreference().first()
            }
            preferenceValue == expected
        }
    }
}
