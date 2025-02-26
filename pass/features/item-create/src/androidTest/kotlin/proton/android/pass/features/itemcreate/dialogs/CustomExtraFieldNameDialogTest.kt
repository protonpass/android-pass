/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemcreate.dialogs

import androidx.compose.ui.test.hasText
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
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.data.api.repositories.DRAFT_NEW_CUSTOM_FIELD_KEY
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.features.itemcreate.R
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameDialog
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldNameNavigation
import proton.android.pass.features.itemcreate.dialogs.customfield.CustomFieldTypeNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.waitUntilExists
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import me.proton.core.presentation.compose.R as CoreR

@HiltAndroidTest
class CustomExtraFieldNameDialogTest {

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
        performTest(
            CustomFieldType.Totp,
            CustomFieldContent.Totp(
                label = LABEL,
                value = HiddenState.Empty(TestEncryptionContext.encrypt(""))
            )
        )
    }

    @Test
    fun testAddHiddenField() {
        performTest(
            CustomFieldType.Hidden,
            CustomFieldContent.Hidden(
                label = LABEL,
                value = HiddenState.Empty(TestEncryptionContext.encrypt(""))
            )
        )
    }

    private fun performTest(type: CustomFieldType, expected: CustomFieldContent) {
        savedStateProvider.get()[CustomFieldTypeNavArgId.key] = type.name

        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    CustomFieldNameDialog(
                        onNavigate = {
                            if (it == CustomFieldNameNavigation.CloseScreen) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val placeholder = activity.getString(R.string.custom_field_dialog_placeholder)
            val confirmText = activity.getString(CoreR.string.presentation_alert_ok)

            waitUntilExists(hasText(placeholder))
            onNodeWithText(placeholder).performTextInput(LABEL)
            onNodeWithText(confirmText).performClick()

            waitUntil { checker.isCalled }
        }

        runBlocking {
            val customField = draftRepository
                .get<CustomFieldContent>(DRAFT_NEW_CUSTOM_FIELD_KEY)
                .first()

            assertNotNull(customField.value())
            assertEquals(expected, customField.value())
        }
    }

    companion object {
        private const val LABEL = "Custom field name"
    }

}
