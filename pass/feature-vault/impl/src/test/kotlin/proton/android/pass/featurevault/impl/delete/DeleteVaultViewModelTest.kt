package proton.android.pass.featurevault.impl.delete

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.fakes.usecases.TestDeleteVault
import proton.android.pass.data.fakes.usecases.TestGetVaultById
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class DeleteVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: DeleteVaultViewModel
    private lateinit var getVaultById: TestGetVaultById
    private lateinit var deleteVault: TestDeleteVault
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher

    @Before
    fun setup() {
        getVaultById = TestGetVaultById()
        deleteVault = TestDeleteVault()
        snackbarDispatcher = TestSnackbarDispatcher()
        instance = DeleteVaultViewModel(
            getVaultById = getVaultById,
            deleteVault = deleteVault,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set(CommonNavArgId.ShareId.key, "123")
            },
            snackbarDispatcher = snackbarDispatcher
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(DeleteVaultUiState.Initial)
        }
    }

    @Test
    fun `emits right vault name`() = runTest {
        performSetup()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.isButtonEnabled).isEqualTo(IsButtonEnabled.Disabled)
            assertThat(item.vaultName).isEqualTo(VAULT_NAME)
            assertThat(item.vaultText).isEqualTo("")
        }
    }

    @Test
    fun `holds text changes`() = runTest {
        performSetup()

        val text = "1234"
        instance.onTextChange(text)
        instance.state.test {
            val item = awaitItem()
            assertThat(item.vaultText).isEqualTo(text)
        }
    }

    @Test
    fun `allows to delete when text matches vault name`() = runTest {
        performSetup()

        instance.onTextChange(VAULT_NAME)
        instance.state.test {
            val item = awaitItem()
            assertThat(item.vaultText).isEqualTo(VAULT_NAME)
            assertThat(item.isButtonEnabled).isEqualTo(IsButtonEnabled.Enabled)
        }
    }

    @Test
    fun `emits success on delete success`() = runTest {
        performSetup()

        deleteVault.setResult(LoadingResult.Success(Unit))
        instance.onDelete()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.event).isEqualTo(DeleteVaultEvent.Deleted)
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isEqualTo(VaultSnackbarMessage.DeleteVaultSuccess)
    }

    @Test
    fun `emits error on delete error`() = runTest {
        performSetup()

        deleteVault.setResult(LoadingResult.Error(IllegalStateException("test")))
        instance.onDelete()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.event).isEqualTo(DeleteVaultEvent.Unknown)
        }

        val message = snackbarDispatcher.snackbarMessage.first().value()!!
        assertThat(message).isEqualTo(VaultSnackbarMessage.DeleteVaultError)
    }

    private fun performSetup() {
        getVaultById.emitValue(testVault())
        instance.onStart()
    }

    private fun testVault(): Vault = Vault(
        shareId = ShareId("123"),
        name = VAULT_NAME,
        color = ShareColor.Color2,
        icon = ShareIcon.Icon1,
        isPrimary = false
    )

    companion object {
        private const val VAULT_NAME = "vault"
    }

}
