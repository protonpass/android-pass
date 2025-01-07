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

package proton.android.pass.features.itemdetail.creditcard

import androidx.annotation.DrawableRes
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.components.SingletonComponent
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.data.api.usecases.ItemWithVaultInfo
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestObserveItemById
import proton.android.pass.data.fakes.usecases.TestObserveItemByIdWithVault
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.domain.VaultId
import proton.android.pass.features.itemdetail.ItemDetailNavigation
import proton.android.pass.features.itemdetail.ItemDetailScreen
import proton.android.pass.features.itemdetail.R
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.CallChecker
import proton.android.pass.test.HiltComponentActivity
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.waitUntilExists
import java.util.Date
import javax.inject.Inject
import kotlin.test.assertEquals
import proton.android.pass.composecomponents.impl.R as CompR

@HiltAndroidTest
class CreditCardDetailScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var savedStateHandle: TestSavedStateHandleProvider

    @Inject
    lateinit var observeItemByIdWithVault: TestObserveItemByIdWithVault

    @Inject
    lateinit var observeItemById: TestObserveItemById

    @Inject
    lateinit var getItemById: FakeGetItemById

    @Inject
    lateinit var clipboardManager: TestClipboardManager

    @Inject
    lateinit var canPerformPaidAction: TestCanPerformPaidAction

    @Inject
    lateinit var observeShare: FakeObserveShare

    @Before
    fun setup() {
        hiltRule.inject()
        savedStateHandle.get().apply {
            set(CommonNavArgId.ShareId.key, SHARE_ID)
            set(CommonNavArgId.ItemId.key, ITEM_ID)
        }
        canPerformPaidAction.setResult(true)
    }

    @Test
    fun displayCreditCardContents() {
        val itemTitle = "a credit card"
        val number = "1234123412341234"
        val note = "some note for the item"
        val expirationDate = "2010-02"
        performSetup(
            title = itemTitle,
            number = number,
            note = note,
            expirationDate = expirationDate
        )
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }
            waitUntilExists(hasText(itemTitle))

            onNode(hasText(itemTitle)).assertExists()
            onNode(hasText("1234 •••• •••• 1234")).assertExists()
            onNode(hasText("02 / 10")).assertExists()
            onNode(hasText(note)).assertExists()
        }
    }

    @Test
    fun toggleCardNumber() {
        val number = "1234123412341234"
        val title = performSetup(number = number)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }
            waitUntilExists(hasText(title))

            val fieldName = activity.getString(R.string.credit_card_number_field_name)
            val contentDescription = activity.getString(R.string.action_reveal_number)

            onNode(hasText(fieldName)).onChildren()
                .filterToOne(hasContentDescription(contentDescription))
                .performClick()

            onNode(hasText("1234 1234 1234 1234")).assertExists()
        }
    }

    @Test
    fun toggleVerificationNumber() {
        val verificationNumber = "987"
        val title = performSetup(verificationNumber = verificationNumber)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }
            waitUntilExists(hasText(title))

            val fieldName = activity.getString(R.string.credit_card_cvv_field_name)
            val contentDescription = activity.getString(R.string.action_reveal_number)

            onNode(hasText(fieldName)).onChildren()
                .filterToOne(hasContentDescription(contentDescription))
                .performClick()

            onNode(hasText(verificationNumber)).assertExists()
        }
    }

    @Test
    fun togglePinNumber() {
        val pin = "4567"
        val title = performSetup(pin = pin)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }
            waitUntilExists(hasText(title))

            val fieldName = activity.getString(R.string.credit_card_pin_field_name)
            onNodeWithText(fieldName).assertExists()

            val contentDescription = activity.getString(R.string.action_reveal_number)
            onAllNodes(hasContentDescription(contentDescription))
                .filterToOne(hasAnySibling(hasText(fieldName)))
                .performClick()

            onNode(hasText(pin)).assertExists()
        }
    }

    @Test
    fun doesNotDisplayEmptyCardHolder() {
        val title = performSetup(cardHolder = "")
        testSectionDoesNotExist(title, R.string.credit_card_cardholder_field_name)
    }

    @Test
    fun doesNotDisplayEmptyCardNumber() {
        val title = performSetup(number = "")
        testSectionDoesNotExist(title, R.string.credit_card_number_field_name)
    }

    @Test
    fun doesNotDisplayEmptyPinNumber() {
        val title = performSetup(pin = "", verificationNumber = "")
        testSectionDoesNotExist(title, R.string.credit_card_pin_field_name)
    }

    @Test
    fun doesNotDisplayEmptyVerificationNumber() {
        val title = performSetup(verificationNumber = "")
        testSectionDoesNotExist(title, R.string.credit_card_cvv_field_name)
    }

    @Test
    fun canCopyCardNumber() {
        val number = "1234123412341234"
        val title = performSetup(number = number)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }
            waitUntilExists(hasText(title))

            val fieldName = activity.getString(R.string.credit_card_number_field_name)
            onNode(hasText(fieldName)).performClick()
        }

        assertEquals(number, clipboardManager.getContents())
    }

    @Test
    fun canCopyVerificationNumber() {
        val verificationNumber = "745"
        val title = performSetup(verificationNumber = verificationNumber)
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }
            waitUntilExists(hasText(title))

            val fieldName = activity.getString(R.string.credit_card_cvv_field_name)
            onNode(hasText(fieldName)).performClick()
        }

        assertEquals(verificationNumber, clipboardManager.getContents())
    }

    @Test
    fun canHandleDowngradedMode() {
        canPerformPaidAction.setResult(false)

        val cardHolder = "Card cardholder"
        val cvv = "333"
        val pin = "6543"
        val expirationDate = "2010-02"
        val formattedExpirationDate = "02 / 10"
        val title = performSetup(
            cardHolder = cardHolder,
            expirationDate = expirationDate,
            pin = pin,
            verificationNumber = cvv
        )
        val checker = CallChecker<Unit>()
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {
                            if (it is ItemDetailNavigation.Upgrade) {
                                checker.call(Unit)
                            }
                        }
                    )
                }
            }
            waitUntilExists(hasText(title))

            // Cardholder is there
            onNodeWithText(cardHolder).assertExists()

            // Expiration date is there
            onNodeWithText(formattedExpirationDate).assertExists()

            // CVV can be revealed
            val contentDescription = activity.getString(R.string.action_reveal_number)
            onAllNodes(hasText("••••")).assertCountEquals(2) // CVV and PIN

            // CVV
            onNode(hasText(cvv)).assertDoesNotExist()
            onAllNodes(hasText("••••"))[0]
                .onChildren()
                .filterToOne(hasContentDescription(contentDescription))
                .performClick()
            onNode(hasText(cvv)).assertExists()

            // PIN
            onNode(hasText(pin)).assertDoesNotExist()
            onNodeWithContentDescription(contentDescription).performClick()
            onNode(hasText(pin)).assertExists()

            // Can go to upgrade
            val upgrade = activity.getString(CompR.string.upgrade)
            onAllNodes(hasText(upgrade)).assertCountEquals(1) // CC number
            onNodeWithText(upgrade).performClick()
            waitUntil { checker.isCalled }
        }
    }

    private fun testSectionDoesNotExist(title: String, @DrawableRes fieldName: Int) {
        composeTestRule.apply {
            setContent {
                PassTheme(isDark = true) {
                    ItemDetailScreen(
                        onNavigate = {}
                    )
                }
            }
            waitUntilExists(hasText(title))

            val fieldNameText = activity.getString(fieldName)
            onNode(hasText(fieldNameText)).assertDoesNotExist()
        }
    }

    private fun performSetup(
        title: String = "some title",
        note: String = "a note",
        cardHolder: String = "cardholder",
        number: String = "1234123412341234",
        pin: String = "1234",
        verificationNumber: String = "123",
        expirationDate: String = "2060-01",
        vaultName: String = "vault"
    ): String {
        val item = TestObserveItems.createCreditCard(
            shareId = ShareId(SHARE_ID),
            itemId = ItemId(ITEM_ID),
            title = title,
            note = note,
            holder = cardHolder,
            number = number,
            pin = pin,
            verificationNumber = verificationNumber,
            expirationDate = expirationDate
        )

        val withVault = ItemWithVaultInfo(
            item = item,
            vaults = listOf(
                Vault(
                    userId = UserId(""),
                    shareId = ShareId(SHARE_ID),
                    vaultId = VaultId("vault-id"),
                    name = vaultName,
                    createTime = Date()
                )
            )
        )

        val share = TestShare.Vault.create(id = SHARE_ID)

        observeItemById.emitValue(Result.success(item))
        observeItemByIdWithVault.emitValue(Result.success(withVault))
        getItemById.emit(Result.success(item))
        observeShare.emitValue(share)
        return title
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object TestModule {
        @Provides
        fun provideClock(): Clock = Clock.System
    }

    companion object {
        private const val SHARE_ID = "shareId"
        private const val ITEM_ID = "itemId"
    }

}
