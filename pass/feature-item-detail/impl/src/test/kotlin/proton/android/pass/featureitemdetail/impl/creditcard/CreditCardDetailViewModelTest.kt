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

package proton.android.pass.featureitemdetail.impl.creditcard

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemWithVaultInfo
import proton.android.pass.data.fakes.repositories.TestBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakePinItem
import proton.android.pass.data.fakes.usecases.FakeUnpinItem
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestCanShareVault
import proton.android.pass.data.fakes.usecases.TestDeleteItems
import proton.android.pass.data.fakes.usecases.TestGetItemActions
import proton.android.pass.data.fakes.usecases.TestGetItemByIdWithVault
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestRestoreItems
import proton.android.pass.data.fakes.usecases.TestTrashItems
import proton.android.pass.data.fakes.usecases.attachments.FakeObserveItemAttachments
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.domain.TestVault

class CreditCardDetailViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: CreditCardDetailViewModel

    private lateinit var getItem: TestGetItemByIdWithVault
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var trashItem: TestTrashItems
    private lateinit var restoreItem: TestRestoreItems
    private lateinit var canPerformPaidAction: TestCanPerformPaidAction
    private lateinit var observeShare: FakeObserveShare

    @Before
    fun setup() {
        getItem = TestGetItemByIdWithVault()
        clipboardManager = TestClipboardManager()
        trashItem = TestTrashItems()
        restoreItem = TestRestoreItems()
        canPerformPaidAction = TestCanPerformPaidAction()
        observeShare = FakeObserveShare()

        instance = CreditCardDetailViewModel(
            snackbarDispatcher = TestSnackbarDispatcher(),
            clipboardManager = clipboardManager,
            encryptionContextProvider = TestEncryptionContextProvider(),
            trashItem = trashItem,
            deleteItem = TestDeleteItems(),
            restoreItem = restoreItem,
            telemetryManager = TestTelemetryManager(),
            canPerformPaidAction = canPerformPaidAction.apply {
                setResult(true)
            },
            getItemByIdWithVault = getItem,
            savedStateHandle = TestSavedStateHandleProvider().apply {
                get().apply {
                    set(CommonNavArgId.ShareId.key, SHARE_ID)
                    set(CommonNavArgId.ItemId.key, ITEM_ID)
                }
            },
            canShareVault = TestCanShareVault(),
            getItemActions = TestGetItemActions(),
            bulkMoveToVaultRepository = TestBulkMoveToVaultRepository(),
            pinItem = FakePinItem(),
            unpinItem = FakeUnpinItem(),
            getUserPlan = TestGetUserPlan(),
            featureFlagsRepository = TestFeatureFlagsPreferenceRepository(),
            observeItemAttachments = FakeObserveItemAttachments(),
            observeShare = observeShare
        )

        observeShare.emitValue(TestShare.Vault.create(id = SHARE_ID))
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.uiState.test {
            assertThat(awaitItem()).isEqualTo(CreditCardDetailUiState.NotInitialised)
        }
    }

    @Test
    fun `emits item`() = runTest {
        val cardNumber = "1234567898765432"
        val holder = "Some card holder"
        val pin = "1111"
        val verificationNumber = "777"
        val expirationDateYear = "2050"
        val expirationDateMonth = "05"
        val expirationDate = "$expirationDateYear-$expirationDateMonth"
        val vaultShare = TestShare.Vault.create()

        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID),
                holder = holder,
                number = cardNumber,
                verificationNumber = verificationNumber,
                pin = pin,
                expirationDate = expirationDate
            ),
            vaults = listOf(
                TEST_VAULT,
                TestVault.create()
            )
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))
        observeShare.emitValue(vaultShare)

        instance.uiState.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(CreditCardDetailUiState.Success::class.java)

            val itemSuccess = item as CreditCardDetailUiState.Success
            assertThat(itemSuccess.isLoading).isFalse()
            assertThat(itemSuccess.share).isEqualTo(vaultShare)
            assertThat(itemSuccess.itemActions).isEqualTo(TestGetItemActions.DEFAULT)
            assertThat(itemSuccess.isItemSentToTrash).isFalse()
            assertThat(itemSuccess.isPermanentlyDeleted).isFalse()
            assertThat(itemSuccess.isRestoredFromTrash).isFalse()

            // Card number
            assertThat(item.itemContent.cardNumber).isInstanceOf(CardNumberState.Masked::class.java)
            val cardNumberState = item.itemContent.cardNumber as CardNumberState.Masked
            assertThat(cardNumberState.number).isEqualTo("1234 •••• •••• 5432")

            // Model
            assertThat(item.itemContent.model.contents).isInstanceOf(ItemContents.CreditCard::class.java)
            val cardContent = item.itemContent.model.contents as ItemContents.CreditCard
            assertThat(cardContent.cardHolder).isEqualTo(holder)
            assertThat(cardContent.pin).isEqualTo(
                HiddenState.Concealed(
                    TestEncryptionContext.encrypt(
                        pin
                    )
                )
            )
            assertThat(cardContent.cvv)
                .isEqualTo(
                    HiddenState.Concealed(TestEncryptionContext.encrypt(verificationNumber))
                )

            val expectedExpiration = "$expirationDateMonth / ${expirationDateYear.substring(2)}"
            assertThat(cardContent.expirationDate).isEqualTo(expectedExpiration)
        }
    }

    @Test
    fun `reveal card number`() = runTest {
        val number = "9876543210987654"
        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID),
                number = number
            ),
            vaults = listOf(TEST_VAULT)
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))

        instance.toggleNumber()
        instance.uiState.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(CreditCardDetailUiState.Success::class.java)
            val itemSuccess = item as CreditCardDetailUiState.Success

            // Card number
            assertThat(itemSuccess.itemContent.cardNumber).isInstanceOf(CardNumberState.Visible::class.java)
            val cardNumberState = item.itemContent.cardNumber as CardNumberState.Visible
            assertThat(cardNumberState.number).isEqualTo("9876 5432 1098 7654")
        }
    }

    @Test
    fun `reveal pin number`() = runTest {
        val pin = "5645"
        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID),
                pin = pin
            ),
            vaults = listOf(TEST_VAULT)
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))

        instance.togglePin()
        instance.uiState.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(CreditCardDetailUiState.Success::class.java)
            val itemSuccess = item as CreditCardDetailUiState.Success

            assertThat(itemSuccess.itemContent.model.contents).isInstanceOf(ItemContents.CreditCard::class.java)
            val cardContent = itemSuccess.itemContent.model.contents as ItemContents.CreditCard
            val expected = HiddenState.Revealed(TestEncryptionContext.encrypt(pin), pin)
            assertThat(cardContent.pin).isEqualTo(expected)
        }
    }

    @Test
    fun `reveal verification number`() = runTest {
        val cvv = "888"
        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID),
                verificationNumber = cvv
            ),
            vaults = listOf(TEST_VAULT)
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))

        instance.toggleCvv()
        instance.uiState.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(CreditCardDetailUiState.Success::class.java)
            val itemSuccess = item as CreditCardDetailUiState.Success

            assertThat(itemSuccess.itemContent.model.contents).isInstanceOf(ItemContents.CreditCard::class.java)
            val cardContent = itemSuccess.itemContent.model.contents as ItemContents.CreditCard
            val expected = HiddenState.Revealed(TestEncryptionContext.encrypt(cvv), cvv)
            assertThat(cardContent.cvv).isEqualTo(expected)
        }
    }

    @Test
    fun `copy card number not toggled`() = runTest {
        val number = "9876543210987654"
        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID),
                number = number
            ),
            vaults = listOf(TEST_VAULT)
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))

        instance.uiState.test {
            skipItems(1)
            instance.copyNumber()
            assertThat(clipboardManager.getContents()).isEqualTo(number)
        }
    }

    @Test
    fun `copy card number toggled`() = runTest {
        val number = "9876543210987654"
        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID),
                number = number
            ),
            vaults = listOf(TEST_VAULT)
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))
        instance.toggleNumber()

        instance.uiState.test {
            skipItems(1)
            instance.copyNumber()
            assertThat(clipboardManager.getContents()).isEqualTo(number)
        }
    }

    @Test
    fun `copy verification number not toggled`() = runTest {
        val cvv = "012"
        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID),
                verificationNumber = cvv
            ),
            vaults = listOf(TEST_VAULT)
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))

        instance.uiState.test {
            skipItems(1)
            instance.copyCvv()
            assertThat(clipboardManager.getContents()).isEqualTo(cvv)
        }
    }

    @Test
    fun `copy verification number toggled`() = runTest {
        val cvv = "876"
        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID),
                verificationNumber = cvv
            ),
            vaults = listOf(TEST_VAULT)
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))
        instance.toggleCvv()

        instance.uiState.test {
            skipItems(1)
            instance.copyCvv()
            assertThat(clipboardManager.getContents()).isEqualTo(cvv)
        }
    }

    @Test
    fun `move to trash`() = runTest {
        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID)
            ),
            vaults = listOf(TEST_VAULT)
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))
        trashItem.setResult(Result.success(Unit))

        instance.onMoveToTrash(ShareId(SHARE_ID), ItemId(ITEM_ID))

        val memory = trashItem.getMemory()
        val expected = TestTrashItems.Payload(
            userId = null,
            items = mapOf(
                ShareId(SHARE_ID) to listOf(ItemId(ITEM_ID))
            )
        )
        assertThat(memory).isEqualTo(listOf(expected))
    }

    @Test
    fun `restore from trash`() = runTest {
        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID)
            ),
            vaults = listOf(TEST_VAULT)
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))
        restoreItem.setResult(Result.success(Unit))

        instance.onItemRestore(ShareId(SHARE_ID), ItemId(ITEM_ID))

        val memory = restoreItem.memory()
        val expected = TestRestoreItems.Payload(
            userId = null,
            items = mapOf(
                ShareId(SHARE_ID) to listOf(ItemId(ITEM_ID))
            )
        )
        assertThat(memory).isEqualTo(listOf(expected))
    }

    @Test
    fun `send isDowngradedMode if in free mode`() = runTest {
        canPerformPaidAction.setResult(false)
        val itemWithVaultInfo = ItemWithVaultInfo(
            item = TestObserveItems.createCreditCard(
                itemId = ItemId(ITEM_ID),
                shareId = ShareId(SHARE_ID)
            ),
            vaults = listOf(TEST_VAULT)
        )
        getItem.emitValue(Result.success(itemWithVaultInfo))

        instance.uiState.test {
            val item = awaitItem()
            assertThat(item).isInstanceOf(CreditCardDetailUiState.Success::class.java)

            val itemSuccess = item as CreditCardDetailUiState.Success
            assertThat(itemSuccess.isDowngradedMode).isTrue()

        }
    }

    companion object {
        private const val SHARE_ID = "shareid-123"
        private const val ITEM_ID = "itemid-456"

        private val TEST_VAULT = TestVault.create(shareId = ShareId(SHARE_ID), name = "Vault")
    }

}
