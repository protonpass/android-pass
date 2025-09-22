/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.settings

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.image.fakes.TestClearIconCache
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UseFaviconsPreference
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import java.util.Date
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

    @Inject
    lateinit var observeVaults: TestObserveVaults

    @Before
    fun setup() {
        hiltRule.inject()
        observeVaults.sendResult(
            Result.success(
                listOf(
                    Vault(
                        userId = UserId(""),
                        shareId = ShareId("1"),
                        vaultId = VaultId("vault-id"),
                        name = "Vault 1",
                        createTime = Date(),
                        shareFlags = ShareFlags(0)
                    ),
                    Vault(
                        userId = UserId(""),
                        shareId = ShareId("2"),
                        vaultId = VaultId("vault-id-2"),
                        name = "Vault 2",
                        createTime = Date(),
                        shareFlags = ShareFlags(0)
                    )
                )
            )
        )
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
    fun onUpClick() {
        val checker = CallChecker<Unit>()
        composeTestRule.setContent {
            PassTheme {
                SettingsScreen(
                    onNavigate = {
                        if (it is SettingsNavigation.CloseScreen) {
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
        preferenceRepository.setUseFaviconsPreference(UseFaviconsPreference.from(initialState))

        preferenceRepository.setThemePreference(ThemePreference.System)
        preferenceRepository.setCopyTotpToClipboardEnabled(CopyTotpToClipboard.Enabled)

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
