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

package proton.android.pass.features.itemcreate.bottomsheets.createitem

import androidx.annotation.StringRes
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.features.itemcreate.R
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import javax.inject.Inject

@HiltAndroidTest
class CreateItemBottomsheetNavItemTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var savedStateHandle: TestSavedStateHandleProvider

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun testCreateLoginFullMode() {
        performTest(
            mode = CreateItemBottomSheetMode.HomeFull,
            text = R.string.item_type_login_description,
        ) { navigation, checker ->
            if (navigation is CreateItemBottomsheetNavigation.CreateLogin) {
                checker.call(Unit)
            }
        }
    }

    @Test
    fun testCreateLoginAutofillLoginMode() {
        performTest(
            mode = CreateItemBottomSheetMode.AutofillLogin,
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
            mode = CreateItemBottomSheetMode.HomeFull,
            text = R.string.item_type_alias_description,
        ) { navigation, checker ->
            if (navigation is CreateItemBottomsheetNavigation.CreateAlias) {
                checker.call(Unit)
            }
        }
    }

    @Test
    fun testCreateAliasAutofillLoginMode() {
        performTest(
            mode = CreateItemBottomSheetMode.AutofillLogin,
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
            mode = CreateItemBottomSheetMode.HomeFull,
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
            mode = CreateItemBottomSheetMode.HomeFull,
            text = R.string.item_type_password_description,
        ) { navigation, checker ->
            if (navigation is CreateItemBottomsheetNavigation.CreatePassword) {
                checker.call(Unit)
            }
        }
    }

    @Test
    fun testCreateCreditCardAutofillCreditCardMode() {
        performTest(
            mode = CreateItemBottomSheetMode.AutofillCreditCard,
            text = R.string.item_type_credit_card_description,
        ) { navigation, checker ->
            if (navigation is CreateItemBottomsheetNavigation.CreateCreditCard) {
                checker.call(Unit)
            }
        }
    }

    private fun performTest(
        mode: CreateItemBottomSheetMode,
        @StringRes text: Int,
        callback: (CreateItemBottomsheetNavigation, CallChecker<Unit>) -> Unit
    ) {
        savedStateHandle.get().apply {
            set(CreateItemBottomSheetModeNavArgId.key, mode)
        }
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme {
                    CreateItemBottomSheet(
                        onNavigate = { callback(it, checker) }
                    )
                }
            }
            onNodeWithText(activity.getString(text)).performClick()
            waitUntil { checker.isCalled }
        }
    }

}
