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

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestObserveDefaultVault
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.attachments.FakeLinkAttachmentsToItem
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.inappreview.fakes.TestInAppReviewTriggerMetrics
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestVault
import proton.android.pass.totp.fakes.TestTotpManager

class CreateCreditCardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: CreateCreditCardViewModel
    private lateinit var totpManager: TestTotpManager
    private lateinit var createItem: TestCreateItem
    private lateinit var observeVaults: TestObserveVaultsWithItemCount
    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var featureFlagsRepository: TestFeatureFlagsPreferenceRepository

    @Before
    fun setUp() {
        totpManager = TestTotpManager()
        createItem = TestCreateItem()
        observeVaults = TestObserveVaultsWithItemCount()
        telemetryManager = TestTelemetryManager()
        snackbarDispatcher = TestSnackbarDispatcher()
        featureFlagsRepository = TestFeatureFlagsPreferenceRepository()
        instance = CreateCreditCardViewModel(
            accountManager = TestAccountManager().apply {
                sendPrimaryUserId(UserId("user-id"))
            },
            createItem = createItem,
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandleProvider = TestSavedStateHandleProvider(),
            encryptionContextProvider = TestEncryptionContextProvider(),
            observeVaults = observeVaults,
            telemetryManager = telemetryManager,
            canPerformPaidAction = TestCanPerformPaidAction().apply { setResult(true) },
            inAppReviewTriggerMetrics = TestInAppReviewTriggerMetrics(),
            observeDefaultVault = TestObserveDefaultVault(),
            featureFlagsRepository = featureFlagsRepository,
            linkAttachmentsToItem = FakeLinkAttachmentsToItem(),
            attachmentsHandler = proton.android.pass.features.itemcreate.attachments.FakeAttachmentHandler()
        )
    }

    @Test
    fun `create item without title should return a BlankTitle validation error`() = runTest {
        val vault = TestVault.create(shareId = ShareId("shareId"), name = "Share")
        val vaultWithItemCount = VaultWithItemCount(
            vault = vault,
            activeItemCount = 1,
            trashedItemCount = 0
        )
        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount)))

        instance.createItem()

        val state = CreateCreditCardUiState.Success(
            shareUiState = ShareUiState.Success(
                vaultList = listOf(vaultWithItemCount),
                currentVault = vaultWithItemCount
            ),
            baseState = BaseCreditCardUiState.Initial
        )

        instance.state.test {
            assertThat(awaitItem()).isEqualTo(
                state.copy(
                    baseState = state.baseState.copy(
                        validationErrors = persistentSetOf(
                            CreditCardValidationErrors.BlankTitle
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `given valid data when a create item event should return a success event`() = runTest {
        val item = TestObserveItems.createCreditCard()
        val vault = sendInitialVault(item.shareId)
        val initialState = CreateCreditCardUiState.Success(
            shareUiState = ShareUiState.Success(
                vaultList = listOf(vault),
                currentVault = vault
            ),
            baseState = BaseCreditCardUiState.Initial
        )
        val titleInput = "Title input"
        instance.onTitleChange(titleInput)
        assertThat(instance.creditCardItemFormState.title).isEqualTo(titleInput)

        createItem.sendItem(Result.success(item))

        instance.state.test {
            instance.createItem()
            val firstItem = awaitItem()
            val firstExpected = initialState.copy(
                baseState = initialState.baseState.copy(
                    isLoading = IsLoadingState.NotLoading.value(),
                    hasUserEditedContent = true
                )
            )
            assertThat(firstItem).isEqualTo(firstExpected)

            val secondItem = awaitItem()
            val secondExpected = firstExpected.copy(
                baseState = firstExpected.baseState.copy(
                    isLoading = IsLoadingState.NotLoading.value(),
                    isItemSaved = ItemSavedState.Success(
                        itemId = item.id,
                        item = ItemUiModel(
                            id = item.id,
                            userId = UserId("user-id"),
                            shareId = item.shareId,
                            contents = toItemContents(
                                itemType = item.itemType,
                                encryptionContext = TestEncryptionContext,
                                title = item.title,
                                note = item.note,
                                flags = item.flags
                            ),
                            createTime = item.createTime,
                            state = ItemState.Active.value,
                            modificationTime = item.modificationTime,
                            lastAutofillTime = item.lastAutofillTime.value(),
                            isPinned = false,
                            category = ItemCategory.CreditCard,
                            revision = item.revision,
                            shareCount = item.shareCount,
                            isOwner = item.isOwner
                        )
                    )
                )
            )
            assertThat(secondItem).isEqualTo(secondExpected)
        }

        val memory = telemetryManager.getMemory()
        assertThat(memory.size).isEqualTo(1)
        assertThat(memory[0]).isEqualTo(ItemCreate(EventItemType.CreditCard))

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isInstanceOf(CreditCardSnackbarMessage.ItemCreated::class.java)
    }

    @Test
    fun `if there is an error creating item a message is emitted`() = runTest {
        val shareId = ShareId("shareId")
        sendInitialVault(shareId)
        createItem.sendItem(Result.failure(IllegalStateException("Test")))

        instance.onTitleChange("Some title")

        instance.state.test {
            instance.createItem()

            val item = awaitItem()
            assertThat(createItem.hasBeenInvoked()).isTrue()

            assertThat(item).isInstanceOf(CreateCreditCardUiState.Success::class.java)
            val casted = item as CreateCreditCardUiState.Success

            assertThat(casted.baseState.isLoading).isEqualTo(IsLoadingState.NotLoading.value())
            assertThat(casted.baseState.isItemSaved).isEqualTo(ItemSavedState.Unknown)

            val message = snackbarDispatcher.snackbarMessage.first().value()!!
            assertThat(message).isInstanceOf(CreditCardSnackbarMessage.ItemCreationError::class.java)
        }
    }

    private fun sendInitialVault(shareId: ShareId): VaultWithItemCount {
        val vault = TestVault.create(shareId = shareId, name = "Share")
        val vaultWithItemCount = VaultWithItemCount(
            vault = vault,
            activeItemCount = 1,
            trashedItemCount = 0
        )
        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount)))
        return vaultWithItemCount
    }

}
