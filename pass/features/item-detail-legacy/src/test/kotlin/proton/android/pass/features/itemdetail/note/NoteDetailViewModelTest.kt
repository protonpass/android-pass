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

package proton.android.pass.features.itemdetail.note

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.commonpresentation.fakes.attachments.FakeAttachmentHandler
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemWithVaultInfo
import proton.android.pass.data.fakes.repositories.TestBulkMoveToVaultRepository
import proton.android.pass.data.fakes.usecases.FakePinItem
import proton.android.pass.data.fakes.usecases.FakeUnpinItem
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestCanShareShare
import proton.android.pass.data.fakes.usecases.TestDeleteItems
import proton.android.pass.data.fakes.usecases.TestGetItemActions
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveItemByIdWithVault
import proton.android.pass.data.fakes.usecases.TestRestoreItems
import proton.android.pass.data.fakes.usecases.TestTrashItems
import proton.android.pass.data.fakes.usecases.attachments.FakeObserveDetailItemAttachments
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.Flags
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import proton.android.pass.features.itemdetail.DetailSnackbarMessages
import proton.android.pass.features.itemdetail.ItemDelete
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.domain.TestShare
import proton.android.pass.test.domain.TestVault

class NoteDetailViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: NoteDetailViewModel

    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var observeItemByIdWithVault: TestObserveItemByIdWithVault
    private lateinit var trashItem: TestTrashItems
    private lateinit var deleteItem: TestDeleteItems
    private lateinit var restoreItem: TestRestoreItems
    private lateinit var encryptionContextProvider: TestEncryptionContextProvider
    private lateinit var canPerformPaidAction: TestCanPerformPaidAction
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var observeShare: FakeObserveShare

    @Before
    fun setup() {
        snackbarDispatcher = TestSnackbarDispatcher()
        telemetryManager = TestTelemetryManager()
        observeItemByIdWithVault = TestObserveItemByIdWithVault()
        trashItem = TestTrashItems()
        deleteItem = TestDeleteItems()
        restoreItem = TestRestoreItems()
        encryptionContextProvider = TestEncryptionContextProvider()
        canPerformPaidAction = TestCanPerformPaidAction()
        clipboardManager = TestClipboardManager()
        observeShare = FakeObserveShare()

        instance = NoteDetailViewModel(
            snackbarDispatcher = snackbarDispatcher,
            telemetryManager = telemetryManager,
            observeItemByIdWithVault = observeItemByIdWithVault,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set(CommonNavArgId.ShareId.key, SHARE_ID)
                set(CommonNavArgId.ItemId.key, ITEM_ID)
            },
            encryptionContextProvider = encryptionContextProvider,
            trashItem = trashItem,
            deleteItem = deleteItem,
            restoreItem = restoreItem,
            canPerformPaidAction = canPerformPaidAction,
            clipboardManager = clipboardManager,
            canShareShare = TestCanShareShare(),
            getItemActions = TestGetItemActions(),
            bulkMoveToVaultRepository = TestBulkMoveToVaultRepository(),
            pinItem = FakePinItem(),
            unpinItem = FakeUnpinItem(),
            getUserPlan = TestGetUserPlan(),
            featureFlagsRepository = TestFeatureFlagsPreferenceRepository(),
            observeItemAttachments = FakeObserveDetailItemAttachments(),
            observeShare = observeShare,
            attachmentsHandler = FakeAttachmentHandler()
        )

        observeShare.emitValue(TestShare.Vault.create())
    }

    @Test
    fun `sends initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(NoteDetailUiState.NotInitialised)
        }
    }

    @Test
    fun `move to trash success`() = runTest {
        val item = initialSetup()

        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isItemSentToTrash).isFalse()
        }

        trashItem.setResult(Result.success(Unit))
        instance.onMoveToTrash(item.shareId, item.id)

        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isItemSentToTrash).isTrue()
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(DetailSnackbarMessages.ItemMovedToTrash)
    }

    @Test
    fun `move to trash error`() = runTest {
        val item = initialSetup()

        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isItemSentToTrash).isFalse()
        }

        trashItem.setResult(Result.failure(IllegalStateException("test")))
        instance.onMoveToTrash(item.shareId, item.id)

        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isItemSentToTrash).isFalse()
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(DetailSnackbarMessages.ItemNotMovedToTrash)
    }

    @Test
    fun `restore from trash success`() = runTest {
        val item = initialSetup()

        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isRestoredFromTrash).isFalse()
        }

        instance.onItemRestore(item.shareId, item.id)
        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isRestoredFromTrash).isTrue()
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(DetailSnackbarMessages.ItemRestored)
    }

    @Test
    fun `restore from trash error`() = runTest {
        val item = initialSetup()

        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isRestoredFromTrash).isFalse()
        }

        restoreItem.setResult(Result.failure(IllegalStateException("test")))
        instance.onItemRestore(item.shareId, item.id)
        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isRestoredFromTrash).isFalse()
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(DetailSnackbarMessages.ItemNotRestored)
    }

    @Test
    fun `permanently delete success`() = runTest {
        val item = initialSetup()

        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isPermanentlyDeleted).isFalse()
        }

        instance.onPermanentlyDelete(item.toUiModel(TestEncryptionContext))
        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isPermanentlyDeleted).isTrue()
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(DetailSnackbarMessages.ItemPermanentlyDeleted)

        val memory = telemetryManager.getMemory()
        assertThat(memory.size).isEqualTo(1)
        assertThat(memory[0]).isEqualTo(ItemDelete(EventItemType.Note))
    }

    @Test
    fun `permanently delete error`() = runTest {
        val item = initialSetup()

        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isPermanentlyDeleted).isFalse()
        }

        deleteItem.setResult(Result.failure(IllegalStateException("test")))
        instance.onPermanentlyDelete(item.toUiModel(TestEncryptionContext))
        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.isPermanentlyDeleted).isFalse()
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()
        assertThat(message).isEqualTo(DetailSnackbarMessages.ItemNotPermanentlyDeleted)

        val memory = telemetryManager.getMemory()
        assertThat(memory).isEmpty()
    }

    @Test
    fun `does not display vault if there is only one vault`() = runTest {
        initialSetup()
        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.hasMoreThanOneVault).isFalse()
        }
    }

    @Test
    fun `shows actions if user is write`() = runTest {
        initialSetup(shareRole = ShareRole.Write)
        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.canPerformActions).isTrue()
        }
    }

    @Test
    fun `shows actions if user is admin`() = runTest {
        initialSetup(shareRole = ShareRole.Admin)
        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.canPerformActions).isTrue()
        }
    }

    @Test
    fun `does not show actions if user is read only`() = runTest {
        initialSetup(shareRole = ShareRole.Read)
        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.canPerformActions).isFalse()
        }
    }

    @Test
    fun `copy note to clipboard copies note to clipboard`() = runTest {
        val noteContents = "note-contents"
        val item = initialSetup(note = noteContents)
        instance.onCopyToClipboard(item.toUiModel(TestEncryptionContext))
        assertThat(clipboardManager.getContents()).isEqualTo(noteContents)
    }

    private fun initialSetup(note: String = "note", shareRole: ShareRole = ShareRole.Admin): Item {
        val item = createEncryptedItem(note = note)
        val value = ItemWithVaultInfo(
            item = item,
            vaults = listOf(TEST_VAULT.copy(role = shareRole))
        )
        observeItemByIdWithVault.emitValue(Result.success(value))
        return item
    }

    private fun createEncryptedItem(title: String = "item-title", note: String = "item-note"): Item {
        val now = Clock.System.now()
        return encryptionContextProvider.withEncryptionContext {
            Item(
                id = ItemId(ITEM_ID),
                userId = UserId("user-id"),
                itemUuid = "uuid",
                revision = 0,
                shareId = ShareId(SHARE_ID),
                itemType = ItemType.Note(note, emptyList()),
                title = encrypt(title),
                note = encrypt(note),
                content = EncryptedByteArray(byteArrayOf()),
                packageInfoSet = emptySet(),
                state = 0,
                modificationTime = now,
                createTime = now,
                lastAutofillTime = None,
                isPinned = false,
                pinTime = None,
                flags = Flags(0),
                shareCount = 0,
                shareType = ShareType.Vault
            )
        }
    }

    companion object {
        private const val SHARE_ID = "share-id"
        private const val ITEM_ID = "item-id"
        private const val VAULT_NAME = "Test Vault"

        private val TEST_VAULT = TestVault.create(
            shareId = ShareId(SHARE_ID),
            name = VAULT_NAME,
            role = ShareRole.Admin
        )
    }

}
