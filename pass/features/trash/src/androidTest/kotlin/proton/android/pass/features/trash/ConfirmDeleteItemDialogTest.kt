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

package proton.android.pass.features.trash

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
                isSharedItem = false,
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
