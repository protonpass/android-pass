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
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.login.CreateUpdateLoginUiState.Companion.Initial
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
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
import proton.pass.domain.VaultWithItemCount

internal class CreateLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var totpManager: TestTotpManager
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var accountManager: TestAccountManager
    private lateinit var createItem: TestCreateItem
    private lateinit var observeVaults: TestObserveVaultsWithItemCount
    private lateinit var createLoginViewModel: CreateLoginViewModel
    private lateinit var telemetryManager: TestTelemetryManager

    @Before
    fun setUp() {
        totpManager = TestTotpManager()
        clipboardManager = TestClipboardManager()
        accountManager = TestAccountManager()
        createItem = TestCreateItem()
        observeVaults = TestObserveVaultsWithItemCount()
        telemetryManager = TestTelemetryManager()
        createLoginViewModel = CreateLoginViewModel(
            accountManager = accountManager,
            createItem = createItem,
            clipboardManager = clipboardManager,
            totpManager = totpManager,
            snackbarDispatcher = TestSnackbarDispatcher(),
            savedStateHandle = TestSavedStateHandle.create(),
            encryptionContextProvider = TestEncryptionContextProvider(),
            createAlias = TestCreateAlias(),
            observeVaults = observeVaults,
            observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) },
            telemetryManager = telemetryManager
        )
    }

    @Test
    fun `when a create item event without title should return a BlankTitle validation error`() =
        runTest {
            val vault = VaultWithItemCount(Vault(ShareId("shareId"), "Share"), 1, 0)
            observeVaults.sendResult(LoadingResult.Success(listOf(vault)))

            createLoginViewModel.createItem()

            createLoginViewModel.loginUiState.test {
                assertThat(awaitItem())
                    .isEqualTo(
                        Initial.copy(
                            vaultList = listOf(vault),
                            selectedVault = vault,
                            validationErrors = setOf(LoginItemValidationErrors.BlankTitle)
                        )
                    )
            }
        }

    @Test
    fun `given valid data when a create item event should return a success event`() = runTest {
        val item = TestItem.create(keyStoreCrypto = TestKeyStoreCrypto)

        val vault = VaultWithItemCount(Vault(item.shareId, "Share"), 1, 0)
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
                        vaultList = listOf(vault),
                        selectedVault = vault,
                        loginItem = LoginItem.Empty.copy(title = titleInput),
                        isLoadingState = IsLoadingState.NotLoading
                    )
                )
            assertThat(awaitItem())
                .isEqualTo(
                    Initial.copy(
                        vaultList = listOf(vault),
                        selectedVault = vault,
                        loginItem = LoginItem.Empty.copy(title = titleInput),
                        isLoadingState = IsLoadingState.Loading
                    )
                )
            assertThat(awaitItem())
                .isEqualTo(
                    Initial.copy(
                        vaultList = listOf(vault),
                        selectedVault = vault,
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

        val memory = telemetryManager.getMemory()
        assertThat(memory.size).isEqualTo(1)
        assertThat(memory[0]).isEqualTo(ItemCreate(EventItemType.Login))
    }

    @Test
    fun `setting initial data emits the proper contents`() = runTest {
        val vault = VaultWithItemCount(Vault(ShareId("shareId"), "Share"), 1, 0)
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
                        vaultList = listOf(
                            VaultWithItemCount(
                                vault = Vault(
                                    vault.vault.shareId,
                                    vault.vault.name,
                                    vault.vault.color,
                                    vault.vault.icon
                                ),
                                activeItemCount = 1,
                                trashedItemCount = 0
                            )
                        ),
                        selectedVault = VaultWithItemCount(
                            vault = Vault(
                                vault.vault.shareId,
                                vault.vault.name,
                                vault.vault.color,
                                vault.vault.icon
                            ),
                            activeItemCount = 1,
                            trashedItemCount = 0
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
