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

import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveUserAccessData
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.itemcreate.R
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.waitUntilExists
import proton.android.pass.test.writeTextAndWait
import java.util.Date
import javax.inject.Inject
import kotlin.test.assertEquals
import proton.android.pass.composecomponents.impl.R as CompR

@HiltAndroidTest
class CreateCreditCardScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var createItem: TestCreateItem

    @Inject
    lateinit var accountManager: TestAccountManager

    @Inject
    lateinit var savedStateHandle: TestSavedStateHandleProvider

    @Inject
    lateinit var observeVaults: TestObserveVaultsWithItemCount

    @Inject
    lateinit var canPerformPaidAction: TestCanPerformPaidAction

    @Inject
    lateinit var observeUserAccessData: TestObserveUserAccessData

    @Before
    fun setup() {
        hiltRule.inject()
        accountManager.sendPrimaryUserId(USER_ID)
        savedStateHandle.get().apply {
            set(CommonNavArgId.ShareId.key, SHARE_ID)
        }

        val vault = VaultWithItemCount(
            vault = Vault(
                userId = USER_ID,
                shareId = ShareId(SHARE_ID),
                vaultId = VaultId("vault-id"),
                name = "Test vault",
                createTime = Date()
            ),
            activeItemCount = 0,
            trashedItemCount = 0
        )
        observeVaults.sendResult(Result.success(listOf(vault)))
        canPerformPaidAction.setResult(true)
        observeUserAccessData.sendValue(null)
    }

    @Test
    fun canCreateCreditCard() {
        val title = "Some title"
        val cardHolder = "John Doe"

        // Do not make it longer, as we add spaces in the view and won't be able to detect them
        val cardNumber = "1234567890123456"
        val formattedCardNumber = "1234 5678 9012 3456"
        val cvv = "987"
        val pin = "1234"

        val expirationMonth = "12"
        val expirationYear = "2030"
        val expirationDate = "${expirationMonth}${expirationYear.substring(2)}"
        val formattedExpirationDate = "$expirationMonth / ${expirationYear.substring(2)}"
        val expirationDateToBeSaved = "${expirationYear}-${expirationMonth}"
        val note = "some note"

        val item = TestObserveItems.createCreditCard(
            title = title,
            holder = cardHolder,
            number = cardNumber,
            verificationNumber = cvv,
            pin = pin,
            note = note,
        )
        createItem.sendItem(Result.success(item))

        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateCreditCardScreen(
                        selectVault = null,
                        onNavigate = {
                            if (it is CreateCreditCardNavigation.ItemCreated) {
                                checker.call()
                            }
                        },
                        canUseAttachments = canUseAttachments
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            val titleText = activity.getString(CompR.string.field_title_title)
            onNode(hasText(titleText)).performScrollTo()
            writeTextAndWait(hasText(titleText), title)

            val cardHolderText = activity.getString(R.string.field_cardholder_name_title)
            onNode(hasText(cardHolderText)).performScrollTo()
            writeTextAndWait(hasText(cardHolderText), cardHolder)

            val cardNumberText = activity.getString(R.string.field_card_number_title)
            onNode(hasText(cardNumberText)).performScrollTo()
            writeTextAndWait(
                matcher = hasText(cardNumberText),
                text = cardNumber,
                expectedText = formattedCardNumber
            )

            val cvvText = activity.getString(R.string.field_card_cvv_title)
            onNode(hasText(cvvText)).performScrollTo()
            writeTextAndWait(hasText(cvvText), cvv)

            val pinText = activity.getString(R.string.field_card_pin_title)
            onNode(hasText(pinText)).performScrollTo()
            writeTextAndWait(hasText(pinText), pin)

            val expirationDateText = activity.getString(R.string.field_card_expiration_date_title)
            onNode(hasText(expirationDateText)).performScrollTo()
            writeTextAndWait(
                matcher = hasText(expirationDateText),
                text = expirationDate,
                expectedText = formattedExpirationDate
            )

            val noteText = activity.getString(CompR.string.field_note_title)
            onNode(hasText(noteText)).performScrollTo()
            writeTextAndWait(hasText(noteText), note)

            Espresso.closeSoftKeyboard()

            onNode(hasText(buttonText)).performClick()

            waitUntil { checker.isCalled }
        }

        val memory = createItem.memory()
        assertEquals(1, memory.size)

        val expected = TestCreateItem.Payload(
            userId = USER_ID,
            shareId = ShareId(SHARE_ID),
            itemContents = ItemContents.CreditCard(
                title = title,
                cardHolder = cardHolder,
                number = cardNumber,
                cvv = HiddenState.Concealed(encrypted= TestEncryptionContext.encrypt(cvv)),
                pin = HiddenState.Concealed(encrypted= TestEncryptionContext.encrypt(pin)),
                note = note,
                expirationDate = expirationDateToBeSaved,
                type = CreditCardType.Other
            )
        )
        assertEquals(listOf(expected), memory)
    }

    @Test
    fun cannotCreateCreditCardWithoutTitle() {
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateCreditCardScreen(
                        selectVault = null,
                        onNavigate = {},
                        canUseAttachments = canUseAttachments
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            onNode(hasText(buttonText)).performClick()

            val errorMessage = activity.getString(CompR.string.field_title_required)
            onNode(hasText(errorMessage)).assertExists()
        }
    }

    @Test
    fun pinIsHiddenWhenFocusIsLost() {
        val pin = "1234"
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateCreditCardScreen(
                        selectVault = null,
                        onNavigate = {},
                        canUseAttachments = canUseAttachments
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            val titleText = activity.getString(CompR.string.field_title_title)

            val pinText = activity.getString(R.string.field_card_pin_title)
            onNode(hasText(pinText)).performScrollTo().performClick()
            writeTextAndWait(hasText(pinText), pin)

            onNode(hasText(titleText)).performScrollTo().performClick()

            Espresso.closeSoftKeyboard()

            onNode(hasText("••••••••")).assertExists()
        }
    }

    @Test
    fun cvvIsHiddenWhenFocusIsLost() {
        val cvv = "789"
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateCreditCardScreen(
                        selectVault = null,
                        onNavigate = {},
                        canUseAttachments = canUseAttachments
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            val titleText = activity.getString(CompR.string.field_title_title)

            val cvvText = activity.getString(R.string.field_card_cvv_title)
            onNode(hasText(cvvText)).performScrollTo().performClick()
            writeTextAndWait(hasText(cvvText), cvv)

            onNode(hasText(titleText)).performScrollTo().performClick()

            Espresso.closeSoftKeyboard()

            onNode(hasText("••••••••")).assertExists()
        }
    }

    @Test
    fun clickOnCloseClosesScreen() {
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    CreateCreditCardScreen(
                        selectVault = null,
                        onNavigate = {
                            if (it == BaseCreditCardNavigation.CloseScreen) {
                                checker.call()
                            }
                        },
                        canUseAttachments = canUseAttachments
                    )
                }
            }

            val buttonText = activity.getString(R.string.title_create)
            waitUntilExists(hasText(buttonText))

            val closeContentDescription = activity.getString(R.string.close_scree_icon_content_description)
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
                    CreateCreditCardScreen(
                        selectVault = null,
                        onNavigate = {
                            if (it == BaseCreditCardNavigation.Upgrade) {
                                checker.call()
                            }
                        },
                        canUseAttachments = canUseAttachments
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
        private val USER_ID = UserId("user-id-123")
    }
}
