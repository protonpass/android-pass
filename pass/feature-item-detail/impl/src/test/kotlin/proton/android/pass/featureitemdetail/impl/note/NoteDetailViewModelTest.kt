package proton.android.pass.featureitemdetail.impl.note

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemWithVaultInfo
import proton.android.pass.data.fakes.usecases.TestDeleteItem
import proton.android.pass.data.fakes.usecases.TestGetItemByIdWithVault
import proton.android.pass.data.fakes.usecases.TestRestoreItem
import proton.android.pass.data.fakes.usecases.TestTrashItem
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages
import proton.android.pass.featureitemdetail.impl.ItemDelete
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class NoteDetailViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: NoteDetailViewModel

    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var getItemByIdWithVault: TestGetItemByIdWithVault
    private lateinit var trashItem: TestTrashItem
    private lateinit var deleteItem: TestDeleteItem
    private lateinit var restoreItem: TestRestoreItem
    private lateinit var encryptionContextProvider: TestEncryptionContextProvider

    @Before
    fun setup() {
        snackbarDispatcher = TestSnackbarDispatcher()
        telemetryManager = TestTelemetryManager()
        getItemByIdWithVault = TestGetItemByIdWithVault()
        trashItem = TestTrashItem()
        deleteItem = TestDeleteItem()
        restoreItem = TestRestoreItem()
        encryptionContextProvider = TestEncryptionContextProvider()
        instance = NoteDetailViewModel(
            snackbarDispatcher = snackbarDispatcher,
            telemetryManager = telemetryManager,
            getItemByIdWithVault = getItemByIdWithVault,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set(CommonNavArgId.ShareId.key, SHARE_ID)
                set(CommonNavArgId.ItemId.key, ITEM_ID)
            },
            encryptionContextProvider = encryptionContextProvider,
            trashItem = trashItem,
            deleteItem = deleteItem,
            restoreItem = restoreItem
        )
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

        trashItem.setResult(LoadingResult.Success(Unit))
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

        trashItem.setResult(LoadingResult.Error(IllegalStateException("test")))
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

        instance.onPermanentlyDelete(item.shareId, item.id, item.itemType)
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
        instance.onPermanentlyDelete(item.shareId, item.id, item.itemType)
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
        initialSetup(hasMoreThanOneVault = false)
        instance.state.test {
            val value = awaitItem() as NoteDetailUiState.Success
            assertThat(value.vault).isNull()
        }
    }

    private fun initialSetup(
        note: String = "note",
        hasMoreThanOneVault: Boolean = true
    ): Item {
        val item = createEncryptedItem(note)
        val value = ItemWithVaultInfo(
            item = item,
            vault = TEST_VAULT,
            hasMoreThanOneVault = hasMoreThanOneVault
        )
        getItemByIdWithVault.emitValue(Result.success(value))
        return item
    }

    private fun createEncryptedItem(
        title: String = "item-title",
        note: String = "item-note",
    ): Item {
        val now = Clock.System.now()
        return encryptionContextProvider.withEncryptionContext {
            Item(
                id = ItemId(ITEM_ID),
                itemUuid = "uuid",
                revision = 0,
                shareId = ShareId(SHARE_ID),
                itemType = ItemType.Note(note),
                title = encrypt(title),
                note = encrypt(note),
                content = EncryptedByteArray(byteArrayOf()),
                packageInfoSet = emptySet(),
                state = 0,
                modificationTime = now,
                createTime = now,
                lastAutofillTime = None
            )
        }
    }

    companion object {
        private const val SHARE_ID = "share-id"
        private const val ITEM_ID = "item-id"
        private const val VAULT_NAME = "Test Vault"

        private val TEST_VAULT = Vault(
            shareId = ShareId(SHARE_ID),
            name = VAULT_NAME,
            color = ShareColor.Color1,
            icon = ShareIcon.Icon1,
            isPrimary = false
        )
    }

}
