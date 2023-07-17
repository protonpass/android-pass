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

package proton.android.pass.featureitemcreate.impl.creditcard

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
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestIncItemCreatedCount
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.totp.fakes.TestTotpManager
import proton.pass.domain.HiddenState
import proton.pass.domain.ItemState
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

class CreateCreditCardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: CreateCreditCardViewModel
    private lateinit var totpManager: TestTotpManager
    private lateinit var createItem: TestCreateItem
    private lateinit var observeVaults: TestObserveVaultsWithItemCount
    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setUp() {
        totpManager = TestTotpManager()
        createItem = TestCreateItem()
        observeVaults = TestObserveVaultsWithItemCount()
        telemetryManager = TestTelemetryManager()
        snackbarDispatcher = TestSnackbarDispatcher()
        instance = CreateCreditCardViewModel(
            accountManager = TestAccountManager().apply {
                sendPrimaryUserId(UserId("user-id"))
            },
            createItem = createItem,
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandle = TestSavedStateHandleProvider(),
            encryptionContextProvider = TestEncryptionContextProvider(),
            observeVaults = observeVaults,
            telemetryManager = telemetryManager,
            canPerformPaidAction = TestCanPerformPaidAction().apply { setResult(true) },
            incItemCreatedCount = TestIncItemCreatedCount()
        )
    }

    @Test
    fun `create item without title should return a BlankTitle validation error`() = runTest {
        val vault = VaultWithItemCount(
            vault = Vault(ShareId("shareId"), "Share", isPrimary = false),
            activeItemCount = 1,
            trashedItemCount = 0
        )
        observeVaults.sendResult(Result.success(listOf(vault)))

        instance.createItem()

        val state = CreateCreditCardUiState.Success(
            shareUiState = ShareUiState.Success(
                vaultList = listOf(vault),
                currentVault = vault
            ),
            baseState = BaseCreditCardUiState.default(
                cvv = HiddenState.Empty(TestEncryptionContext.encrypt("")),
                pin = HiddenState.Empty(TestEncryptionContext.encrypt("")),
            )
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
            baseState = BaseCreditCardUiState.default(
                cvv = HiddenState.Empty(TestEncryptionContext.encrypt("")),
                pin = HiddenState.Empty(TestEncryptionContext.encrypt("")),
            )
        )
        val titleInput = "Title input"
        instance.onTitleChange(titleInput)

        createItem.sendItem(Result.success(item))

        instance.state.test {
            instance.createItem()
            val firstItem = awaitItem()
            val firstExpected = initialState.copy(
                baseState = initialState.baseState.copy(
                    contents = initialState.baseState.contents.copy(title = titleInput),
                    isLoading = IsLoadingState.NotLoading.value(),
                    hasUserEditedContent = true,
                )
            )
            assertThat(firstItem).isEqualTo(firstExpected)

            val secondItem = awaitItem()
            val secondExpected = firstExpected.copy(
                baseState = firstExpected.baseState.copy(
                    isLoading = IsLoadingState.Loading.value(),
                    contents = firstExpected.baseState.contents.copy(
                        pin = HiddenState.Empty(TestEncryptionContext.encrypt("")),
                        cvv = HiddenState.Empty(TestEncryptionContext.encrypt(""))
                    )
                )
            )
            assertThat(secondItem).isEqualTo(secondExpected)

            val thirdItem = awaitItem()
            val thirdExpected = secondExpected.copy(
                baseState = secondExpected.baseState.copy(
                    isLoading = IsLoadingState.NotLoading.value(),
                    isItemSaved = ItemSavedState.Success(
                        itemId = item.id,
                        item = ItemUiModel(
                            id = item.id,
                            shareId = item.shareId,
                            contents = item.toItemContents(TestEncryptionContext),
                            createTime = item.createTime,
                            state = ItemState.Active.value,
                            modificationTime = item.modificationTime,
                            lastAutofillTime = item.lastAutofillTime.value()
                        )
                    )
                )
            )
            assertThat(thirdItem).isEqualTo(thirdExpected)
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

            skipItems(2)
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
        val vault = VaultWithItemCount(
            vault = Vault(shareId, "Share", isPrimary = false),
            activeItemCount = 1,
            trashedItemCount = 0
        )
        observeVaults.sendResult(Result.success(listOf(vault)))
        return vault
    }

}
