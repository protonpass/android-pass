package me.proton.android.pass.featurecreateitem.impl.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.android.pass.commonuimodels.api.ItemUiModel
import me.proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import me.proton.android.pass.crypto.fakes.context.TestEncryptionContext
import me.proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import me.proton.android.pass.data.fakes.usecases.TestCreateAlias
import me.proton.android.pass.data.fakes.usecases.TestCreateItem
import me.proton.android.pass.data.fakes.usecases.TestObserveActiveShare
import me.proton.android.pass.featurecreateitem.impl.ItemSavedState
import me.proton.android.pass.featurecreateitem.impl.login.CreateUpdateLoginUiState.Companion.Initial
import me.proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.commonui.api.itemName
import me.proton.pass.domain.ShareId
import me.proton.pass.test.MainDispatcherRule
import me.proton.pass.test.TestAccountManager
import me.proton.pass.test.TestSavedStateHandle
import me.proton.pass.test.TestUtils
import me.proton.pass.test.crypto.TestKeyStoreCrypto
import me.proton.pass.test.domain.TestItem
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
            savedStateHandle = TestSavedStateHandle.create(),
            encryptionContextProvider = TestEncryptionContextProvider(),
            createAlias = TestCreateAlias()
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
        val item = TestItem.create(keyStoreCrypto = TestKeyStoreCrypto)
        createItem.sendItem(Result.Success(item))

        createLoginViewModel.createItem(item.shareId)

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
                        isItemSaved = ItemSavedState.Success(
                            item.id,
                            ItemUiModel(
                                id = item.id,
                                shareId = item.shareId,
                                name = item.itemName(TestEncryptionContext),
                                note = TestEncryptionContext.decrypt(item.note),
                                itemType = item.itemType
                            )
                        )
                    )
                )
        }
    }

    @Test
    fun `setting initial data emits the proper contents`() = runTest {
        val initialContents = InitialCreateLoginUiState(
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
