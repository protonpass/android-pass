package proton.android.pass.featurecreateitem.impl.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.Result
import proton.android.pass.commonui.api.itemName
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestObserveActiveShare
import proton.android.pass.featurecreateitem.impl.ItemSavedState
import proton.android.pass.featurecreateitem.impl.login.CreateUpdateLoginUiState.Companion.Initial
import proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestAccountManager
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.TestUtils
import proton.android.pass.test.crypto.TestKeyStoreCrypto
import proton.android.pass.test.domain.TestItem
import proton.pass.domain.ShareId

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
                                itemType = item.itemType,
                                modificationTime = item.modificationTime
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
