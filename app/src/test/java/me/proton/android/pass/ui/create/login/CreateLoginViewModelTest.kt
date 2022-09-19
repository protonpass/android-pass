package me.proton.android.pass.ui.create.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.android.pass.ui.MainDispatcherRule
import me.proton.android.pass.ui.create.login.CreateUpdateLoginUiState.Companion.Initial
import me.proton.android.pass.ui.shared.uievents.IsLoadingState
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.test.core.TestAccountManager
import me.proton.core.pass.test.domain.TestItem
import me.proton.core.pass.test.domain.usecases.TestCreateItem
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class CreateLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var accountManager: TestAccountManager
    private lateinit var createItem: TestCreateItem
    private lateinit var createLoginViewModel: CreateLoginViewModel

    @Before
    fun setUp() {
        accountManager = TestAccountManager()
        createItem = TestCreateItem()
        createLoginViewModel = CreateLoginViewModel(
            accountManager = accountManager,
            createItem = createItem
        )
    }

    @Test
    fun `when a create item event without title should return a BlankTitle validation error`() =
        runTest {
            val shareId = ShareId("id")

            createLoginViewModel.createItem(shareId)

            createLoginViewModel.loginUiState.test {
                assertThat(awaitItem())
                    .isEqualTo(Initial.copy(errorList = listOf(LoginItemValidationErrors.BlankTitle)))
            }
        }

    @Test
    fun `given valid data when a create item event should return a success event`() = runTest {
        val titleInput = "Title input"
        createLoginViewModel.onTitleChange(titleInput)

        val userId = UserId("user-id")
        accountManager.sendPrimaryUserId(userId)
        val item = TestItem.create()
        createItem.sendItem(item)
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
}
