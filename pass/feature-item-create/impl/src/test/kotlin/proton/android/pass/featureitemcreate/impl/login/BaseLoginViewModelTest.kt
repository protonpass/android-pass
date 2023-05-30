package proton.android.pass.featureitemcreate.impl.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.fakes.TestTotpManager
import proton.pass.domain.HiddenState

internal class BaseLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var totpManager: TestTotpManager
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var observeCurrentUser: TestObserveCurrentUser
    private lateinit var baseLoginViewModel: BaseLoginViewModel

    private val initial = BaseLoginUiState.create(
        HiddenState.Concealed(TestEncryptionContext.encrypt("")),
        HiddenState.Revealed(TestEncryptionContext.encrypt(""), "")
    )

    @Before
    fun setUp() {
        totpManager = TestTotpManager()
        clipboardManager = TestClipboardManager()
        observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) }
        baseLoginViewModel = object : BaseLoginViewModel(
            accountManager = TestAccountManager(),
            snackbarDispatcher = TestSnackbarDispatcher(),
            clipboardManager = clipboardManager,
            totpManager = totpManager,
            draftRepository = TestDraftRepository(),
            observeCurrentUser = observeCurrentUser,
            observeUpgradeInfo = TestObserveUpgradeInfo(),
            encryptionContextProvider = TestEncryptionContextProvider(),
            ffRepo = TestFeatureFlagsPreferenceRepository()
        ) {}
    }

    @Test
    fun `should start with the initial state`() = runTest {
        baseLoginViewModel.baseLoginUiState.test {
            assertThat(awaitItem()).isEqualTo(
                initial.copy(
                    totpUiState = TotpUiState.Success,
                    customFieldsState = CustomFieldsState.Disabled,
                )
            )
        }
    }

    @Test
    fun `when the title has changed the state should hold it`() = runTest {
        val titleInput = "Title Changed"
        baseLoginViewModel.onTitleChange(titleInput)
        baseLoginViewModel.baseLoginUiState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(initial.contents.copy(title = titleInput))
        }
    }

    @Test
    fun `when the username has changed the state should hold it`() = runTest {
        val usernameInput = "Username Changed"
        baseLoginViewModel.onUsernameChange(usernameInput)
        baseLoginViewModel.baseLoginUiState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(initial.contents.copy(username = usernameInput))
        }
    }

    @Test
    fun `when the password has changed the state should hold it`() = runTest {
        val passwordInput = "Password Changed"
        val encryptedPassword = TestEncryptionContext.encrypt("Password Changed")
        baseLoginViewModel.onPasswordChange(passwordInput)
        baseLoginViewModel.baseLoginUiState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(
                    initial.contents.copy(
                        password = HiddenState.Revealed(encryptedPassword, passwordInput)
                    )
                )
        }
    }

    @Test
    fun `when the note has changed the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        baseLoginViewModel.onNoteChange(noteInput)
        baseLoginViewModel.baseLoginUiState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(initial.contents.copy(note = noteInput))
        }
    }

    @Test
    fun `when a website has been changed the state should change it`() = runTest {
        val url = "proton.me"
        baseLoginViewModel.onWebsiteChange(url, 0)
        baseLoginViewModel.baseLoginUiState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(initial.contents.copy(urls = listOf(url)))
        }
    }

    @Test
    fun `when a website has been added the state should add it`() = runTest {
        baseLoginViewModel.onAddWebsite()
        baseLoginViewModel.baseLoginUiState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(initial.contents.copy(urls = listOf("", "")))
        }
    }

    @Test
    fun `when a website has been removed the state should remove it`() = runTest {
        baseLoginViewModel.onRemoveWebsite(0)
        baseLoginViewModel.baseLoginUiState.test {
            assertThat(awaitItem().contents)
                .isEqualTo(initial.contents.copy(urls = emptyList()))
        }
    }
}
