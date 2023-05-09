package proton.android.pass.featureitemcreate.impl.login

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.usecases.TestGetUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.login.CreateUpdateLoginUiState.Companion.Initial
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestAccountManager
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.fakes.TestTotpManager
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

internal class BaseLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var totpManager: TestTotpManager
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var observeVaults: TestObserveVaultsWithItemCount
    private lateinit var observeCurrentUser: TestObserveCurrentUser
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var baseLoginViewModel: BaseLoginViewModel

    @Before
    fun setUp() {
        totpManager = TestTotpManager()
        clipboardManager = TestClipboardManager()
        observeVaults = TestObserveVaultsWithItemCount()
        observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) }
        savedStateHandle = TestSavedStateHandle.create()
        baseLoginViewModel = object : BaseLoginViewModel(
            accountManager = TestAccountManager(),
            snackbarDispatcher = TestSnackbarDispatcher(),
            clipboardManager = clipboardManager,
            totpManager = totpManager,
            draftRepository = TestDraftRepository(),
            observeVaults = observeVaults,
            observeCurrentUser = observeCurrentUser,
            savedStateHandle = savedStateHandle,
            getUpgradeInfo = TestGetUpgradeInfo(),
            encryptionContextProvider = TestEncryptionContextProvider()
        ) {}
    }

    @Test
    fun `should start with the initial state`() = runTest {
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem()).isEqualTo(Initial.copy(totpUiState = TotpUiState.Success))
        }
    }

    @Test
    fun `when the title has changed the state should hold it`() = runTest {
        val titleInput = "Title Changed"
        givenAVaultList()
        baseLoginViewModel.onTitleChange(titleInput)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(title = titleInput))
        }
    }

    @Test
    fun `when the username has changed the state should hold it`() = runTest {
        val usernameInput = "Username Changed"
        givenAVaultList()
        baseLoginViewModel.onUsernameChange(usernameInput)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(username = usernameInput))
        }
    }

    @Test
    fun `when the password has changed the state should hold it`() = runTest {
        val passwordInput = "Password Changed"
        givenAVaultList()
        baseLoginViewModel.onPasswordChange(passwordInput)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(password = passwordInput))
        }
    }

    @Test
    fun `when the note has changed the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        givenAVaultList()
        baseLoginViewModel.onNoteChange(noteInput)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(note = noteInput))
        }
    }

    @Test
    fun `when a website has been changed the state should change it`() = runTest {
        val url = "proton.me"
        givenAVaultList()
        baseLoginViewModel.onWebsiteChange(url, 0)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(websiteAddresses = listOf(url)))
        }
    }

    @Test
    fun `when a website has been added the state should add it`() = runTest {
        givenAVaultList()
        baseLoginViewModel.onAddWebsite()
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(websiteAddresses = listOf("", "")))
        }
    }

    @Test
    fun `when a website has been removed the state should remove it`() = runTest {
        givenAVaultList()
        baseLoginViewModel.onRemoveWebsite(0)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(websiteAddresses = emptyList()))
        }
    }

    private fun givenAVaultList() {
        observeVaults.sendResult(
            Result.success(
                listOf(
                    VaultWithItemCount(
                        vault = Vault(ShareId("ShareId"), "name", isPrimary = false),
                        activeItemCount = 1,
                        trashedItemCount = 0
                    )
                )
            )
        )
    }
}
