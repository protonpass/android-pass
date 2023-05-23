package proton.android.pass.featuremigrate.impl.selectvault

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.featuremigrate.impl.MigrateModeArg
import proton.android.pass.featuremigrate.impl.MigrateModeValue
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

class MigrateSelectVaultForMigrateItemViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateSelectVaultViewModel
    private lateinit var observeVaults: TestObserveVaultsWithItemCount
    private lateinit var observeUpgradeInfo: TestObserveUpgradeInfo
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        observeVaults = TestObserveVaultsWithItemCount()
        observeUpgradeInfo = TestObserveUpgradeInfo()
        snackbarDispatcher = TestSnackbarDispatcher()
        instance = MigrateSelectVaultViewModel(
            observeVaults = observeVaults,
            observeUpgradeInfo = observeUpgradeInfo,
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set(CommonNavArgId.ShareId.key, SHARE_ID.id)
                set(MigrateModeArg.key, MODE.name)

                set(CommonOptionalNavArgId.ItemId.key, ITEM_ID.id)
            }
        )
    }

    @Test
    fun `emits success when vault selected`() = runTest {
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(Result.success(listOf(currentVault, otherVault)))

        instance.onVaultSelected(otherVault.vault.shareId)
        instance.state.test {
            val item = awaitItem()
            require(item is MigrateSelectVaultUiState.Success)
            val event = item.event.value()

            assertThat(event).isNotNull()
            assertThat(event!!).isInstanceOf(SelectVaultEvent.VaultSelectedForMigrateItem::class.java)

            val castedEvent = event as SelectVaultEvent.VaultSelectedForMigrateItem
            assertThat(castedEvent.itemId).isEqualTo(ITEM_ID)
            assertThat(castedEvent.sourceShareId).isEqualTo(SHARE_ID)
            assertThat(castedEvent.destinationShareId).isEqualTo(otherVault.vault.shareId)
        }
    }

    private fun initialVaults(): Pair<VaultWithItemCount, VaultWithItemCount> =
        Pair(
            VaultWithItemCount(
                vault = Vault(
                    shareId = SHARE_ID,
                    name = "vault1",
                    isPrimary = false
                ),
                activeItemCount = 1,
                trashedItemCount = 0
            ),
            VaultWithItemCount(
                vault = Vault(
                    shareId = ShareId("OTHER_SHARE_ID"),
                    name = "vault2",
                    isPrimary = false
                ),
                activeItemCount = 1,
                trashedItemCount = 0
            )
        )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val ITEM_ID = ItemId("456")

        private val MODE = MigrateModeValue.SingleItem
    }
}
