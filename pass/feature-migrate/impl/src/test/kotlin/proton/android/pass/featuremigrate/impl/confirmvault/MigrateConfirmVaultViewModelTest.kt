package proton.android.pass.featuremigrate.impl.confirmvault

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.Some
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.fakes.usecases.TestGetVaultWithItemCountById
import proton.android.pass.data.fakes.usecases.TestMigrateItem
import proton.android.pass.featuremigrate.impl.MigrateSnackbarMessage
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.domain.TestItem
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

class MigrateConfirmVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateConfirmVaultViewModel
    private lateinit var migrateItem: TestMigrateItem
    private lateinit var getVaultById: TestGetVaultWithItemCountById
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        migrateItem = TestMigrateItem()
        snackbarDispatcher = TestSnackbarDispatcher()
        getVaultById = TestGetVaultWithItemCountById()
        instance =
            MigrateConfirmVaultViewModel(
                migrateItem = migrateItem,
                snackbarDispatcher = snackbarDispatcher,
                getVaultById = getVaultById,
                savedStateHandle = TestSavedStateHandle.create().apply {
                    set(CommonNavArgId.ShareId.key, SHARE_ID.id)
                    set(CommonNavArgId.ItemId.key, ITEM_ID.id)
                    set(DestinationShareNavArgId.key, DESTINATION_SHARE_ID.id)
                }
            )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(MigrateConfirmVaultUiState.Initial)
        }
    }

    @Test
    fun `stops loading when vault has emitted`() = runTest {
        val vault = sourceVault()
        getVaultById.emitValue(vault)
        instance.state.test {
            val secondState = awaitItem()
            assertThat(secondState.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
            assertThat(secondState.vault.isNotEmpty()).isTrue()

            val itemVault = secondState.vault.value()!!
            assertThat(itemVault).isEqualTo(vault)
        }
    }

    @Test
    fun `emits close if there is an error in get vault`() = runTest {
        getVaultById.sendException(IllegalStateException("test"))
        instance.state.test {
            val state = awaitItem()
            assertThat(state.event.isNotEmpty()).isTrue()

            val eventCasted = state.event as Some<ConfirmMigrateEvent>
            assertThat(eventCasted.value).isInstanceOf(ConfirmMigrateEvent.Close::class.java)
        }
    }

    @Test
    fun `emits close if cancel is clicked`() = runTest {
        getVaultById.emitValue(sourceVault())
        instance.onCancel()
        instance.state.test {
            val state = awaitItem()
            val eventCasted = state.event as Some<ConfirmMigrateEvent>
            assertThat(eventCasted.value).isInstanceOf(ConfirmMigrateEvent.Close::class.java)

            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun `can migrate items`() = runTest {
        getVaultById.emitValue(sourceVault())

        val expectedItem = TestItem.create().copy(id = ITEM_ID, shareId = DESTINATION_SHARE_ID)
        migrateItem.setResult(Result.success(expectedItem))

        instance.onConfirm()
        instance.state.test {
            val state = awaitItem()
            val eventCasted = state.event as Some<ConfirmMigrateEvent>
            assertThat(eventCasted.value).isInstanceOf(ConfirmMigrateEvent.ItemMigrated::class.java)

            val migratedEvent = eventCasted.value as ConfirmMigrateEvent.ItemMigrated
            assertThat(migratedEvent.itemId).isEqualTo(expectedItem.id)
            assertThat(migratedEvent.shareId).isEqualTo(expectedItem.shareId)

            val snackbarMessage = snackbarDispatcher.snackbarMessage.first()
            assertThat(snackbarMessage.isNotEmpty()).isTrue()

            val message = snackbarMessage.value()!!
            assertThat(message).isInstanceOf(MigrateSnackbarMessage.ItemMigrated::class.java)
        }
    }

    @Test
    fun `displays error if cannot migrate items`() = runTest {
        getVaultById.emitValue(sourceVault())
        migrateItem.setResult(Result.failure(IllegalStateException("test")))

        instance.onConfirm()
        instance.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)

            val snackbarMessage = snackbarDispatcher.snackbarMessage.first()
            assertThat(snackbarMessage.isNotEmpty()).isTrue()

            val message = snackbarMessage.value()!!
            assertThat(message).isInstanceOf(MigrateSnackbarMessage.ItemNotMigrated::class.java)
        }
    }


    private fun sourceVault(): VaultWithItemCount = VaultWithItemCount(
        vault = Vault(
            shareId = SHARE_ID,
            name = "source",
            isPrimary = false
        ),
        activeItemCount = 1,
        trashedItemCount = 0
    )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val DESTINATION_SHARE_ID = ShareId("456")
        private val ITEM_ID = ItemId("789")
    }


}
