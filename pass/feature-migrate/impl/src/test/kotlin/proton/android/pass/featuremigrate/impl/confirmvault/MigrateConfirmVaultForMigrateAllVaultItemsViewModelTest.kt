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
import proton.android.pass.data.fakes.usecases.TestMigrateVault
import proton.android.pass.featuremigrate.impl.MigrateModeArg
import proton.android.pass.featuremigrate.impl.MigrateModeValue
import proton.android.pass.featuremigrate.impl.MigrateSnackbarMessage
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.DestinationShareNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

class MigrateConfirmVaultForMigrateAllVaultItemsViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateConfirmVaultViewModel
    private lateinit var migrateItem: TestMigrateItem
    private lateinit var migrateVault: TestMigrateVault
    private lateinit var getVaultById: TestGetVaultWithItemCountById
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        migrateItem = TestMigrateItem()
        migrateVault = TestMigrateVault()
        snackbarDispatcher = TestSnackbarDispatcher()
        getVaultById = TestGetVaultWithItemCountById()
        instance = MigrateConfirmVaultViewModel(
            migrateItem = migrateItem,
            migrateVault = migrateVault,
            snackbarDispatcher = snackbarDispatcher,
            getVaultById = getVaultById,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set(CommonNavArgId.ShareId.key, SHARE_ID.id)
                set(DestinationShareNavArgId.key, DESTINATION_SHARE_ID.id)
                set(MigrateModeArg.key, MODE.name)
            }
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            val expected = MigrateConfirmVaultUiState.Initial(MigrateMode.MigrateAll).copy(
                isLoading = IsLoadingState.Loading // Retrieve vault is loading
            )
            assertThat(awaitItem()).isEqualTo(expected)
        }
    }

    @Test
    fun `can migrate items`() = runTest {
        getVaultById.emitValue(sourceVault())

        instance.onConfirm()
        instance.state.test {
            val state = awaitItem()
            val eventCasted = state.event as Some<ConfirmMigrateEvent>
            assertThat(eventCasted.value).isInstanceOf(ConfirmMigrateEvent.AllItemsMigrated::class.java)
        }

        val snackbarMessage = snackbarDispatcher.snackbarMessage.first()
        assertThat(snackbarMessage.isNotEmpty()).isTrue()

        val message = snackbarMessage.value()!!
        assertThat(message).isInstanceOf(MigrateSnackbarMessage.VaultItemsMigrated::class.java)

        val expected = TestMigrateVault.Memory(SHARE_ID, DESTINATION_SHARE_ID)
        assertThat(migrateVault.memory()).isEqualTo(listOf(expected))
    }

    @Test
    fun `displays error if cannot migrate items`() = runTest {
        getVaultById.emitValue(sourceVault())
        migrateVault.setResult(Result.failure(IllegalStateException("test")))

        instance.onConfirm()
        instance.state.test {
            val state = awaitItem()
            assertThat(state.isLoading).isInstanceOf(IsLoadingState.NotLoading::class.java)
        }

        val snackbarMessage = snackbarDispatcher.snackbarMessage.first()
        assertThat(snackbarMessage.isNotEmpty()).isTrue()

        val message = snackbarMessage.value()!!
        assertThat(message).isInstanceOf(MigrateSnackbarMessage.VaultItemsNotMigrated::class.java)

        val expected = TestMigrateVault.Memory(SHARE_ID, DESTINATION_SHARE_ID)
        assertThat(migrateVault.memory()).isEqualTo(listOf(expected))
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

        private val MODE = MigrateModeValue.AllVaultItems
    }
}
