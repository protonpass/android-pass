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

package proton.android.pass.features.itemcreate.creditcard

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestObserveItemById
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveUserAccessData
import proton.android.pass.data.fakes.usecases.TestUpdateItem
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import proton.android.pass.features.itemcreate.R
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.waitUntilExists
import proton.android.pass.test.writeTextAndWait
import javax.inject.Inject
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import proton.android.pass.composecomponents.impl.R as CompR

@HiltAndroidTest
class UpdateCreditCardScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var updateItem: TestUpdateItem

    @Inject
    lateinit var accountManager: TestAccountManager

    @Inject
    lateinit var savedStateHandle: TestSavedStateHandleProvider

    @Inject
    lateinit var getItemById: TestObserveItemById

    @Inject
    lateinit var canPerformPaidAction: TestCanPerformPaidAction

    @Inject
    lateinit var observeUserAccessData: TestObserveUserAccessData

    private lateinit var initialItem: Item

    @Before
    fun setup() {
        hiltRule.inject()
        accountManager.sendPrimaryUserId(USER_ID)
        savedStateHandle.get().apply {
            set(CommonNavArgId.ShareId.key, SHARE_ID)
            set(CommonNavArgId.ItemId.key, ITEM_ID)
        }

        initialItem = TestObserveItems.createCreditCard(
            shareId = ShareId(SHARE_ID),
            title = DEFAULT_TITLE,
            note = DEFAULT_NOTE,
            holder = DEFAULT_CARDHOLDER,
            number = DEFAULT_NUMBER,
            pin = DEFAULT_PIN,
            verificationNumber = DEFAULT_VERIFICATION_NUMBER,
            expirationDate = ITEM_EXPIRATION_DATE,
        )
        getItemById.emitValue(Result.success(initialItem))
        updateItem.setResult(Result.success(initialItem))
        canPerformPaidAction.setResult(true)
        observeUserAccessData.sendValue(null)
    }

    @Test
    fun initialContentsAreProperlySet() {
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    UpdateCreditCardScreen(onNavigate = {})
                }
            }

            waitUntilExists(hasText(DEFAULT_TITLE))

            onNodeWithText(DEFAULT_TITLE).assertExists()
            onNodeWithText(DEFAULT_CARDHOLDER).assertExists()
            onNodeWithText(DEFAULT_FORMATTED_NUMBER).assertExists()

            onAllNodes(hasText(HIDDEN_FIELD_VALUE)).assertCountEquals(2)

            onNodeWithText(FORMATTED_EXPIRATION_DATE).assertExists()
            onNodeWithText(DEFAULT_NOTE).assertExists()
        }
    }

    @Test
    fun canUpdateCreditCard() {
        val newTitle = "New title"
        val newCardHolder = "Another card holder"
        val newNumber = "9999888877776666"
        val newFormattedNumber = "9999 8888 7777 6666"
        val newPin = "555"
        val newCvv = "777"
        val newExpirationDateMonth = "04"
        val newExpirationDateYear = "2043"
        val newExpirationDate = "${newExpirationDateMonth}${newExpirationDateYear.substring(2)}"
        val newFormattedExpirationDate =
            "$newExpirationDateMonth / ${newExpirationDateYear.substring(2)}"
        val newItemExpirationDate = "${newExpirationDateYear}-${newExpirationDateMonth}"
        val newNote = "Updated note"

        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    UpdateCreditCardScreen(onNavigate = {
                        if (it is UpdateCreditCardNavigation.ItemUpdated) {
                            checker.call()
                        }
                    })
                }
            }

            waitUntilExists(hasText(DEFAULT_TITLE))

            // Title
            val titleText = activity.getString(CompR.string.field_title_title)
            onNode(hasText(titleText)).performScrollTo()
            writeTextAndWait(hasText(titleText), newTitle)

            // Cardholder
            val cardHolderText = activity.getString(R.string.field_cardholder_name_title)
            onNode(hasText(cardHolderText)).performScrollTo()
            writeTextAndWait(matcher = hasText(cardHolderText), text = newCardHolder)

            // Number
            val numberText = activity.getString(R.string.field_card_number_title)
            onNode(hasText(numberText)).performScrollTo()
            writeTextAndWait(
                matcher = hasText(numberText),
                text = newNumber,
                expectedText = newFormattedNumber
            )

            val cvvText = activity.getString(R.string.field_card_cvv_title)
            onNode(hasText(cvvText)).performScrollTo().performClick()
            writeTextAndWait(hasText(cvvText), newCvv)

            // PIN
            val pinText = activity.getString(R.string.field_card_pin_title)
            onNode(hasText(pinText)).performScrollTo().performClick()
            writeTextAndWait(hasText(pinText), newPin)

            // Expiration date
            val dateText = activity.getString(R.string.field_card_expiration_date_title)
            onNode(hasText(dateText)).performScrollTo().performClick()
            writeTextAndWait(
                matcher = hasText(dateText),
                text = newExpirationDate,
                expectedText = newFormattedExpirationDate
            )

            // Note
            onNodeWithText(DEFAULT_NOTE).performScrollTo().performClick().performTextClearance()
            val noteText = activity.getString(CompR.string.field_note_title)
            onNode(hasText(noteText)).performScrollTo().performClick()
            writeTextAndWait(matcher = hasText(noteText), text = newNote)

            waitForIdle()

            Espresso.closeSoftKeyboard()

            // Submit
            val buttonText = activity.getString(R.string.action_save)
            onNodeWithText(buttonText).performClick()

            waitUntil { checker.isCalled }
        }

        val memory = updateItem.getMemory()
        assertEquals(1, memory.size)

        val expected = TestUpdateItem.Payload(
            userId = USER_ID,
            shareId = ShareId(SHARE_ID),
            item = initialItem,
            contents = ItemContents.CreditCard(
                title = newTitle,
                note = newNote,
                cardHolder = newCardHolder,
                number = newNumber,
                pin = HiddenState.Concealed(TestEncryptionContext.encrypt(newPin)),
                cvv = HiddenState.Concealed(TestEncryptionContext.encrypt(newCvv)),
                expirationDate = newItemExpirationDate,
                type = CreditCardType.Other,
                customFields = emptyList()
            )
        )

        assertEquals(listOf(expected), memory)
    }


    @Test
    fun cannotUpdateCreditCardWithoutTitle() {
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    UpdateCreditCardScreen(onNavigate = {})
                }
            }

            waitUntilExists(hasText(DEFAULT_TITLE))

            onNodeWithText(DEFAULT_TITLE).performTextClearance()

            val buttonText = activity.getString(R.string.action_save)
            onNode(hasText(buttonText)).performClick()

            val errorMessage = activity.getString(CompR.string.field_title_required)
            onNode(hasText(errorMessage)).assertExists()
        }

        assertTrue(updateItem.getMemory().isEmpty())
    }

    @Test
    fun clickOnCloseClosesScreen() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    UpdateCreditCardScreen(
                        onNavigate = {
                            if (it == BaseCreditCardNavigation.CloseScreen) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val buttonText = activity.getString(R.string.action_save)
            waitUntilExists(hasText(buttonText))

            val closeContentDescription =
                activity.getString(R.string.close_scree_icon_content_description)
            onNode(hasContentDescription(closeContentDescription)).performClick()

            waitUntil { checker.isCalled }
        }
    }

    @Test
    fun canHandleDowngradedMode() {
        canPerformPaidAction.setResult(false)
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    UpdateCreditCardScreen(
                        onNavigate = {
                            if (it == BaseCreditCardNavigation.Upgrade) {
                                checker.call()
                            }
                        }
                    )
                }
            }

            val buttonText = activity.getString(R.string.upgrade)
            waitUntilExists(hasText(buttonText))
            onNodeWithText(buttonText).performClick()

            waitUntil { checker.isCalled }
        }
    }

    companion object {
        private const val SHARE_ID = "shareId-123"
        private const val ITEM_ID = "itemId-456"

        private const val DEFAULT_TITLE = "Card title"
        private const val DEFAULT_CARDHOLDER = "Some cardholder"
        private const val DEFAULT_NUMBER = "1234567890123456"
        private const val DEFAULT_FORMATTED_NUMBER = "1234 5678 9012 3456"
        private const val DEFAULT_PIN = "1234"
        private const val DEFAULT_VERIFICATION_NUMBER = "123"
        private const val DEFAULT_NOTE = "This is some note"
        private const val HIDDEN_FIELD_VALUE = "••••••••"
        private const val DEFAULT_EXPIRATION_DATE_MONTH = "12"
        private const val DEFAULT_EXPIRATION_DATE_YEAR = "2056"
        private const val ITEM_EXPIRATION_DATE =
            "$DEFAULT_EXPIRATION_DATE_YEAR-$DEFAULT_EXPIRATION_DATE_MONTH"
        private val FORMATTED_EXPIRATION_DATE =
            "$DEFAULT_EXPIRATION_DATE_MONTH / ${DEFAULT_EXPIRATION_DATE_YEAR.substring(2)}"

        private val USER_ID = UserId("user-id-123")
    }

}
