package proton.android.pass.featureitemcreate.impl.login

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.featureitemcreate.impl.login.CreateUpdateLoginUiState.Companion.Initial
import proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestAccountManager
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.fakes.TestTotpManager
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

internal class BaseLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var totpManager: TestTotpManager
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var observeVaults: TestObserveVaults
    private lateinit var observeCurrentUser: TestObserveCurrentUser
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var baseLoginViewModel: BaseLoginViewModel

    @Before
    fun setUp() {
        totpManager = TestTotpManager()
        clipboardManager = TestClipboardManager()
        observeVaults = TestObserveVaults()
        observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) }
        savedStateHandle = TestSavedStateHandle.create()
        baseLoginViewModel = object : BaseLoginViewModel(
            TestCreateAlias(),
            TestAccountManager(),
            TestSnackbarMessageRepository(),
            clipboardManager,
            totpManager,
            observeVaults,
            observeCurrentUser,
            savedStateHandle
        ) {}
    }

    @Test
    fun `should start with the initial state`() = runTest {
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem()).isEqualTo(Initial)
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
        observeVaults.sendResult(LoadingResult.Success(listOf(Vault(ShareId("shareId"), "Share"))))
    }
}
