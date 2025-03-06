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
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.errors.InvalidContentFormatVersionError
import proton.android.pass.data.api.repositories.PendingAttachmentLinkRepository
import proton.android.pass.data.fakes.repositories.FakePendingAttachmentLinkRepository
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestObserveItemById
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestUpdateItem
import proton.android.pass.data.fakes.usecases.attachments.FakeLinkAttachmentsToItem
import proton.android.pass.data.fakes.usecases.attachments.FakeRenameAttachments
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.ItemUpdate
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.preferences.TestPreferenceRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule

class UpdateCreditCardViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: UpdateCreditCardViewModel

    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var getItemById: TestObserveItemById
    private lateinit var updateItem: TestUpdateItem
    private lateinit var accountManager: TestAccountManager
    private lateinit var featureFlagsRepository: TestFeatureFlagsPreferenceRepository
    private lateinit var pendingAttachmentLinkRepository: PendingAttachmentLinkRepository

    @Before
    fun setup() {
        telemetryManager = TestTelemetryManager()
        snackbarDispatcher = TestSnackbarDispatcher()
        getItemById = TestObserveItemById()
        updateItem = TestUpdateItem()
        featureFlagsRepository = TestFeatureFlagsPreferenceRepository()
        pendingAttachmentLinkRepository = FakePendingAttachmentLinkRepository()
        accountManager = TestAccountManager()
        accountManager.sendPrimaryUserId(UserId("user-id"))
        instance = UpdateCreditCardViewModel(
            accountManager = accountManager,
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandleProvider = TestSavedStateHandleProvider().apply {
                get()[CommonOptionalNavArgId.ShareId.key] = SHARE_ID
                get()[CommonNavArgId.ItemId.key] = ITEM_ID
            },
            encryptionContextProvider = TestEncryptionContextProvider(),
            telemetryManager = telemetryManager,
            getItemById = getItemById,
            updateItem = updateItem,
            canPerformPaidAction = TestCanPerformPaidAction().apply { setResult(true) },
            featureFlagsRepository = featureFlagsRepository,
            attachmentsHandler = proton.android.pass.features.itemcreate.attachments.FakeAttachmentHandler(),
            linkAttachmentsToItem = FakeLinkAttachmentsToItem(),
            renameAttachments = FakeRenameAttachments(),
            userPreferencesRepository = TestPreferenceRepository(),
            pendingAttachmentLinkRepository = pendingAttachmentLinkRepository
        )
    }

    @Test
    fun `update item without title should return a BlankTitle validation error`() = runTest {
        val item = TestObserveItems.createCreditCard(title = "")
        getItemById.emitValue(Result.success(item))

        instance.update()
        instance.state.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(UpdateCreditCardUiState.Success::class.java)

            val casted = state as UpdateCreditCardUiState.Success
            val expected = persistentSetOf(CreditCardValidationErrors.BlankTitle)
            assertThat(casted.baseState.validationErrors).isEqualTo(expected)
        }
    }

    @Test
    fun `can update with valid contents`() = runTest {
        val item = TestObserveItems.createCreditCard(title = "title")
        getItemById.emitValue(Result.success(item))
        instance.onTitleChange("TitleChanged") // there needs to be a change to trigger an update
        updateItem.setResult(Result.success(item))

        instance.update()
        instance.state.test {

            val state = awaitItem()
            assertThat(state).isInstanceOf(UpdateCreditCardUiState.Success::class.java)

            val casted = state as UpdateCreditCardUiState.Success
            assertThat(casted.baseState.isLoading).isEqualTo(IsLoadingState.NotLoading.value())
            assertThat(casted.baseState.isItemSaved).isInstanceOf(ItemSavedState.Success::class.java)

            val castedEvent = casted.baseState.isItemSaved as ItemSavedState.Success
            assertThat(castedEvent.itemId).isEqualTo(item.id)
        }

        val updateMemory = updateItem.getMemory()
        assertThat(updateMemory.size).isEqualTo(1)
        assertThat(updateMemory[0].item.id).isEqualTo(item.id)

        val telemetryMemory = telemetryManager.getMemory()
        assertThat(telemetryMemory.size).isEqualTo(1)
        assertThat(telemetryMemory[0]).isEqualTo(ItemUpdate(EventItemType.CreditCard))

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isInstanceOf(CreditCardSnackbarMessage.ItemUpdated::class.java)
    }

    @Test
    fun `if there is an error updating item a message is emitted`() = runTest {
        runTestError(IllegalStateException("Test"))

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isInstanceOf(CreditCardSnackbarMessage.ItemCreationError::class.java)
    }

    @Test
    fun `if error is InvalidContentFormatVersionError shows right snackbar message`() = runTest {
        runTestError(InvalidContentFormatVersionError())
        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isInstanceOf(CreditCardSnackbarMessage.UpdateAppToUpdateItemError::class.java)
    }

    private suspend fun runTestError(exception: Throwable) {
        val item = TestObserveItems.createCreditCard(title = "title")
        getItemById.emitValue(Result.success(item))
        instance.onTitleChange("TitleChanged") // there needs to be a change to trigger an update
        updateItem.setResult(Result.failure(exception))

        instance.update()
        instance.state.test {
            val state = awaitItem()
            assertThat(state).isInstanceOf(UpdateCreditCardUiState.Success::class.java)

            val casted = state as UpdateCreditCardUiState.Success
            assertThat(casted.baseState.isLoading).isEqualTo(IsLoadingState.NotLoading.value())
            assertThat(casted.baseState.isItemSaved).isEqualTo(ItemSavedState.Unknown)
        }


        val updateMemory = updateItem.getMemory()
        assertThat(updateMemory.size).isEqualTo(1)
        assertThat(updateMemory[0].item.id).isEqualTo(item.id)

        val telemetryMemory = telemetryManager.getMemory()
        assertThat(telemetryMemory).isEmpty()
    }

    companion object {
        private const val SHARE_ID = "shareId"
        private const val ITEM_ID = "itemId"
    }
}
