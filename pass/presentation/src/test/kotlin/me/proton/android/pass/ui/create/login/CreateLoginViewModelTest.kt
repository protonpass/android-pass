package me.proton.android.pass.ui.create.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.android.pass.ui.MainDispatcherRule
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ShareId
import me.proton.pass.presentation.create.login.CreateLoginViewModel
import me.proton.pass.presentation.create.login.CreateUpdateLoginUiState.Companion.Initial
import me.proton.pass.presentation.create.login.InitialCreateLoginContents
import me.proton.pass.presentation.create.login.LoginItem
import me.proton.pass.presentation.create.login.LoginItemValidationErrors
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState
import me.proton.pass.test.TestUtils
import me.proton.pass.test.core.TestAccountManager
import me.proton.pass.test.core.TestSavedStateHandle
import me.proton.pass.test.domain.TestItem
import me.proton.pass.test.domain.usecases.TestCreateItem
import me.proton.pass.test.domain.usecases.TestObserveActiveShare
import me.proton.pass.test.notification.TestSnackbarMessageRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class CreateLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var accountManager: TestAccountManager
    private lateinit var createItem: TestCreateItem
    private lateinit var observeActiveShare: TestObserveActiveShare
    private lateinit var createLoginViewModel: CreateLoginViewModel

    @Before
    fun setUp() {
        accountManager = TestAccountManager()
        createItem = TestCreateItem()
        observeActiveShare = TestObserveActiveShare()
        createLoginViewModel = CreateLoginViewModel(
            accountManager = accountManager,
            createItem = createItem,
            observeActiveShare = observeActiveShare,
            snackbarMessageRepository = TestSnackbarMessageRepository(),
            savedStateHandle = TestSavedStateHandle.create()
        )
    }

    @Test
    fun `when a create item event without title should return a BlankTitle validation error`() =
        runTest {
            val shareId = ShareId("id")

            createLoginViewModel.createItem(shareId)

            createLoginViewModel.loginUiState.test {
                assertThat(awaitItem())
                    .isEqualTo(Initial.copy(validationErrors = setOf(LoginItemValidationErrors.BlankTitle)))
            }
        }

    @Test
    fun `given valid data when a create item event should return a success event`() = runTest {
        val titleInput = "Title input"
        createLoginViewModel.onTitleChange(titleInput)

        val userId = UserId("user-id")
        accountManager.sendPrimaryUserId(userId)
        val item = TestItem.create()
        createItem.sendItem(Result.Success(item))
        val shareId = ShareId("id")

        createLoginViewModel.createItem(shareId)

        createLoginViewModel.loginUiState.test {
            assertThat(awaitItem())
                .isEqualTo(
                    Initial.copy(
                        loginItem = LoginItem.Empty.copy(title = titleInput),
                        isLoadingState = IsLoadingState.Loading
                    )
                )
            assertThat(awaitItem())
                .isEqualTo(
                    Initial.copy(
                        loginItem = LoginItem.Empty.copy(title = titleInput),
                        isLoadingState = IsLoadingState.NotLoading,
                        isItemSaved = ItemSavedState.Success(item.id)
                    )
                )
        }
    }

    @Test
    fun `setting initial data emits the proper contents`() = runTest {
        val initialContents = InitialCreateLoginContents(
            title = TestUtils.randomString(),
            username = TestUtils.randomString(),
            password = TestUtils.randomString(),
            url = TestUtils.randomString()
        )
        createLoginViewModel.setInitialContents(initialContents)

        createLoginViewModel.loginUiState.test {
            assertThat(awaitItem())
                .isEqualTo(
                    Initial.copy(
                        loginItem = LoginItem.Empty.copy(
                            title = initialContents.title!!,
                            username = initialContents.username!!,
                            password = initialContents.password!!,
                            websiteAddresses = listOf(initialContents.url!!)
                        )
                    )
                )
        }
    }
}
