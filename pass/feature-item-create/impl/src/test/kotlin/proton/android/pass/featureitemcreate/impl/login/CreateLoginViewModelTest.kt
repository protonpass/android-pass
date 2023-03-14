package proton.android.pass.featureitemcreate.impl.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.commonui.api.itemName
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.login.CreateUpdateLoginUiState.Companion.Initial
import proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestAccountManager
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.TestUtils
import proton.android.pass.test.crypto.TestKeyStoreCrypto
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.fakes.TestTotpManager
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

internal class CreateLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var totpManager: TestTotpManager
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var accountManager: TestAccountManager
    private lateinit var createItem: TestCreateItem
    private lateinit var observeVaults: TestObserveVaults
    private lateinit var createLoginViewModel: CreateLoginViewModel

    @Before
    fun setUp() {
        totpManager = TestTotpManager()
        clipboardManager = TestClipboardManager()
        accountManager = TestAccountManager()
        createItem = TestCreateItem()
        observeVaults = TestObserveVaults()
        createLoginViewModel = CreateLoginViewModel(
            accountManager = accountManager,
            createItem = createItem,
            clipboardManager = clipboardManager,
            totpManager = totpManager,
            snackbarMessageRepository = TestSnackbarMessageRepository(),
            savedStateHandle = TestSavedStateHandle.create(),
            encryptionContextProvider = TestEncryptionContextProvider(),
            createAlias = TestCreateAlias(),
            observeVaults = observeVaults,
            observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) }
        )
    }

    @Test
    fun `when a create item event without title should return a BlankTitle validation error`() =
        runTest {
            val vault = Vault(ShareId("shareId"), "Share")
            observeVaults.sendResult(LoadingResult.Success(listOf(vault)))

            createLoginViewModel.createItem()

            createLoginViewModel.loginUiState.test {
                assertThat(awaitItem())
                    .isEqualTo(
                        Initial.copy(
                            shareList = listOf(ShareUiModel(vault.shareId, vault.name, vault.color, vault.icon)),
                            selectedShareId = ShareUiModel(vault.shareId, vault.name, vault.color, vault.icon),
                            validationErrors = setOf(LoginItemValidationErrors.BlankTitle)
                        )
                    )
            }
        }

    @Test
    fun `given valid data when a create item event should return a success event`() = runTest {
        val item = TestItem.create(keyStoreCrypto = TestKeyStoreCrypto)

        val vault = Vault(item.shareId, "Share")
        observeVaults.sendResult(LoadingResult.Success(listOf(vault)))

        val titleInput = "Title input"
        createLoginViewModel.onTitleChange(titleInput)

        val userId = UserId("user-id")
        accountManager.sendPrimaryUserId(userId)
        createItem.sendItem(LoadingResult.Success(item))

        createLoginViewModel.loginUiState.test {
            createLoginViewModel.createItem()
            assertThat(awaitItem())
                .isEqualTo(
                    Initial.copy(
                        shareList = listOf(
                            ShareUiModel(
                                vault.shareId,
                                vault.name,
                                vault.color,
                                vault.icon
                            )
                        ),
                        selectedShareId = ShareUiModel(
                            vault.shareId,
                            vault.name,
                            vault.color,
                            vault.icon
                        ),
                        loginItem = LoginItem.Empty.copy(title = titleInput),
                        isLoadingState = IsLoadingState.NotLoading
                    )
                )
            assertThat(awaitItem())
                .isEqualTo(
                    Initial.copy(
                        shareList = listOf(
                            ShareUiModel(
                                vault.shareId,
                                vault.name,
                                vault.color,
                                vault.icon
                            )
                        ),
                        selectedShareId = ShareUiModel(
                            vault.shareId,
                            vault.name,
                            vault.color,
                            vault.icon
                        ),
                        loginItem = LoginItem.Empty.copy(title = titleInput),
                        isLoadingState = IsLoadingState.Loading
                    )
                )
            assertThat(awaitItem())
                .isEqualTo(
                    Initial.copy(
                        shareList = listOf(
                            ShareUiModel(
                                vault.shareId,
                                vault.name,
                                vault.color,
                                vault.icon
                            )
                        ),
                        selectedShareId = ShareUiModel(
                            vault.shareId,
                            vault.name,
                            vault.color,
                            vault.icon
                        ),
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
                                createTime = item.createTime,
                                modificationTime = item.modificationTime,
                                lastAutofillTime = item.lastAutofillTime.value()
                            )
                        )
                    )
                )
        }
    }

    @Test
    fun `setting initial data emits the proper contents`() = runTest {
        val vault = Vault(ShareId("shareId"), "Share")
        observeVaults.sendResult(LoadingResult.Success(listOf(vault)))
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
                        shareList = listOf(
                            ShareUiModel(
                                vault.shareId,
                                vault.name,
                                vault.color,
                                vault.icon
                            )
                        ),
                        selectedShareId = ShareUiModel(
                            vault.shareId,
                            vault.name,
                            vault.color,
                            vault.icon
                        ),
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
