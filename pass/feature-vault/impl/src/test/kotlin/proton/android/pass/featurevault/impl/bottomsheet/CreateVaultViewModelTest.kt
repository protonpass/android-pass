package proton.android.pass.featurevault.impl.bottomsheet

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.fakes.usecases.TestCreateVault
import proton.android.pass.data.fakes.usecases.TestGetUpgradeInfo
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestShare
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

class CreateVaultViewModelTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var instance: CreateVaultViewModel
    private lateinit var snackbar: TestSnackbarDispatcher
    private lateinit var createVault: TestCreateVault
    private lateinit var getUpgradeInfo: TestGetUpgradeInfo

    @Before
    fun setup() {
        snackbar = TestSnackbarDispatcher()
        createVault = TestCreateVault()
        getUpgradeInfo = TestGetUpgradeInfo().apply {
            setResult(TestGetUpgradeInfo.DEFAULT)
        }
        instance = CreateVaultViewModel(
            snackbar,
            createVault,
            TestEncryptionContextProvider(),
            getUpgradeInfo
        )
    }

    @Test
    fun `emits initial state`() = runTest {
        instance.state.test {
            assertThat(awaitItem()).isEqualTo(BaseVaultUiState.Initial)
        }
    }

    @Test
    fun `holds name changes`() = runTest {
        val name = "test"
        instance.onNameChange(name)
        instance.state.test {
            assertThat(awaitItem().name).isEqualTo(name)
        }
    }

    @Test
    fun `holds icon changes`() = runTest {
        val icon = ShareIcon.Icon8
        instance.onIconChange(icon)
        instance.state.test {
            assertThat(awaitItem().icon).isEqualTo(icon)
        }
    }

    @Test
    fun `holds color changes`() = runTest {
        val color = ShareColor.Color7
        instance.onColorChange(color)
        instance.state.test {
            assertThat(awaitItem().color).isEqualTo(color)
        }
    }

    @Test
    fun `entering and clearing the text disables button and shows error`() = runTest {
        instance.onNameChange("name")
        instance.onNameChange("")
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isCreateButtonEnabled).isEqualTo(IsButtonEnabled.Disabled)
            assertThat(item.isTitleRequiredError).isTrue()
        }
    }

    @Test
    fun `displays error snackbar on createVault error`() = runTest {
        instance.onNameChange("name")

        createVault.setResult(Result.failure(IllegalStateException("test")))
        instance.onCreateClick()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isVaultCreatedEvent).isEqualTo(IsVaultCreatedEvent.Unknown)
            assertThat(item.isLoading).isEqualTo(IsLoadingState.NotLoading)

            val message = snackbar.snackbarMessage.first().value()
            assertThat(message).isNotNull()
            assertThat(message).isEqualTo(VaultSnackbarMessage.CreateVaultError)
        }
    }

    @Test
    fun `displays proper error snackbar on cannotCreateMoreVaults`() = runTest {
        instance.onNameChange("name")

        createVault.setResult(Result.failure(CannotCreateMoreVaultsError()))
        instance.onCreateClick()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isVaultCreatedEvent).isEqualTo(IsVaultCreatedEvent.Unknown)
            assertThat(item.isLoading).isEqualTo(IsLoadingState.NotLoading)

            val message = snackbar.snackbarMessage.first().value()
            assertThat(message).isNotNull()
            assertThat(message).isEqualTo(VaultSnackbarMessage.CannotCreateMoreVaultsError)
        }
    }

    @Test
    fun `displays success snackbar on createVault success`() = runTest {
        instance.onNameChange("name")

        createVault.setResult(Result.success(TestShare.create()))
        instance.onCreateClick()
        instance.state.test {
            val item = awaitItem()
            assertThat(item.isVaultCreatedEvent).isEqualTo(IsVaultCreatedEvent.Created)
            assertThat(item.isLoading).isEqualTo(IsLoadingState.NotLoading)

            val message = snackbar.snackbarMessage.first().value()
            assertThat(message).isNotNull()
            assertThat(message).isEqualTo(VaultSnackbarMessage.CreateVaultSuccess)
        }
    }

    @Test
    fun `does not display upgrade ui if vault limit not reached`() = runTest {
        getUpgradeInfo.setResult(
            TestGetUpgradeInfo.DEFAULT.copy(
                isUpgradeAvailable = true,
                totalVaults = 1,
                vaultLimit = 2
            )
        )
        instance.createState.test {
            val item = awaitItem()
            assertThat(item.displayNeedUpgrade).isFalse()
        }
    }

    @Test
    fun `does not display upgrade ui if vault limit reached but upgrade unavailable`() = runTest {
        getUpgradeInfo.setResult(
            TestGetUpgradeInfo.DEFAULT.copy(
                isUpgradeAvailable = false,
                totalVaults = 1,
                vaultLimit = 1
            )
        )
        instance.createState.test {
            val item = awaitItem()
            assertThat(item.displayNeedUpgrade).isFalse()
        }
    }

    @Test
    fun `displays upgrade ui if plan is free`() = runTest {
        getUpgradeInfo.setResult(
            TestGetUpgradeInfo.DEFAULT.copy(
                isUpgradeAvailable = true,
                totalVaults = 1,
                vaultLimit = 1
            )
        )
        instance.createState.test {
            val item = awaitItem()
            assertThat(item.displayNeedUpgrade).isTrue()
        }
    }
}
