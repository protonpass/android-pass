package proton.android.pass.featureitemdetail.impl.migrate.selectvault

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

class MigrateSelectVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: MigrateSelectVaultViewModel
    private lateinit var observeVaults: TestObserveVaultsWithItemCount

    @Before
    fun setup() {
        observeVaults = TestObserveVaultsWithItemCount()
        instance = MigrateSelectVaultViewModel(
            observeVaults = observeVaults,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set(CommonNavArgId.ShareId.key, SHARE_ID.id)
                set(CommonNavArgId.ItemId.key, ITEM_ID.id)
            }
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(MigrateSelectVaultUiState.Initial)
        }
    }

    @Test
    fun `marks the current vault as not enabled`() = runTest {
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(LoadingResult.Success(listOf(currentVault, otherVault)))

        val expected = listOf(
            VaultEnabledPair(currentVault, false),
            VaultEnabledPair(otherVault, true)
        )
        instance.state.test {
            val item = awaitItem()
            assertThat(item.vaultList).isEqualTo(expected)
            assertThat(item.event.value()).isNull()
        }
    }

    @Test
    fun `emits success when vault selected`() = runTest {
        val (currentVault, otherVault) = initialVaults()
        observeVaults.sendResult(LoadingResult.Success(listOf(currentVault, otherVault)))

        instance.onVaultSelected(otherVault.vault.shareId)
        instance.state.test {
            val event = awaitItem().event.value()
            assertThat(event).isNotNull()
            assertThat(event!!).isInstanceOf(SelectVaultEvent.SelectedVault::class.java)

            val castedEvent = event as SelectVaultEvent.SelectedVault
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
                    name = "vault1"
                ),
                activeItemCount = 1,
                trashedItemCount = 0
            ),
            VaultWithItemCount(
                vault = Vault(
                    shareId = ShareId("OTHER_SHARE_ID"),
                    name = "vault2"
                ),
                activeItemCount = 1,
                trashedItemCount = 0
            )
        )

    companion object {
        private val SHARE_ID = ShareId("123")
        private val ITEM_ID = ItemId("456")
    }

}
