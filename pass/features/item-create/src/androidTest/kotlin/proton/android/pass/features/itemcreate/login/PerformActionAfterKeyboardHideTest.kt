/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.itemcreate.login

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity

@HiltAndroidTest
class PerformActionAfterKeyboardHideTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun clearsFocusBeforePerformingAction() {
        // Below API 28 the framework re-assigns focus to the root view after clearFocus(),
        // regardless of the APK's targetSdk, so the cleared state cannot be observed there.
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)

        val checker = CallChecker<Unit>()

        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    var text by remember { mutableStateOf("") }
                    var action by remember { mutableStateOf<(() -> Unit)?>(null) }
                    PerformActionAfterKeyboardHide(
                        action = action,
                        clearAction = { action = null }
                    )
                    Column {
                        BasicTextField(
                            modifier = Modifier.testTag(FIELD_TAG),
                            value = text,
                            onValueChange = { text = it }
                        )
                        Button(onClick = { action = { checker.call() } }) {
                            Text(text = BUTTON_TEXT)
                        }
                    }
                }
            }

            onNodeWithTag(FIELD_TAG).performClick()
            onNodeWithTag(FIELD_TAG).assertIsFocused()

            onNodeWithText(BUTTON_TEXT).performClick()

            waitUntil { checker.isCalled }
            onNodeWithTag(FIELD_TAG).assertIsNotFocused()
        }
    }

    private companion object {
        private const val FIELD_TAG = "field"
        private const val BUTTON_TEXT = "Navigate"
    }
}
