package proton.android.pass.featurecreateitem.impl.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import proton.android.pass.featurecreateitem.impl.login.CreateUpdateLoginUiState.Companion.Initial
import proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.TestAccountManager
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestObserveActiveShare
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class BaseLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var baseLoginViewModel: BaseLoginViewModel

    @Before
    fun setUp() {
        baseLoginViewModel = object : BaseLoginViewModel(
            TestCreateAlias(),
            TestAccountManager(),
            TestSnackbarMessageRepository(),
            TestObserveActiveShare(),
            TestSavedStateHandle.create()
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
        baseLoginViewModel.onTitleChange(titleInput)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(title = titleInput))
        }
    }

    @Test
    fun `when the username has changed the state should hold it`() = runTest {
        val usernameInput = "Username Changed"
        baseLoginViewModel.onUsernameChange(usernameInput)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(username = usernameInput))
        }
    }

    @Test
    fun `when the password has changed the state should hold it`() = runTest {
        val passwordInput = "Password Changed"
        baseLoginViewModel.onPasswordChange(passwordInput)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(password = passwordInput))
        }
    }

    @Test
    fun `when the note has changed the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        baseLoginViewModel.onNoteChange(noteInput)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(note = noteInput))
        }
    }

    @Test
    fun `when a website has been changed the state should change it`() = runTest {
        val url = "proton.me"
        baseLoginViewModel.onWebsiteChange(url, 0)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(websiteAddresses = listOf(url)))
        }
    }

    @Test
    fun `when a website has been added the state should add it`() = runTest {
        baseLoginViewModel.onAddWebsite()
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(websiteAddresses = listOf("", "")))
        }
    }

    @Test
    fun `when a website has been removed the state should remove it`() = runTest {
        baseLoginViewModel.onRemoveWebsite(0)
        baseLoginViewModel.loginUiState.test {
            assertThat(awaitItem().loginItem)
                .isEqualTo(Initial.loginItem.copy(websiteAddresses = emptyList()))
        }
    }
}
