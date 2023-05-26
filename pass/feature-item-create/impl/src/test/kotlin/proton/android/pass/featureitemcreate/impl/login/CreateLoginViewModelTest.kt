package proton.android.pass.featureitemcreate.impl.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.commonui.api.itemName
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestCreateItemAndAlias
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.alias.AliasMailboxUiModel
import proton.android.pass.featureitemcreate.impl.alias.AliasOptionsUiModel
import proton.android.pass.featureitemcreate.impl.alias.AliasSuffixUiModel
import proton.android.pass.featureitemcreate.impl.alias.SelectedAliasMailboxUiModel
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestFeatureFlagsPreferenceRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.TestUtils
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.fakes.TestTotpManager
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

internal class CreateLoginViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: CreateLoginViewModel
    private lateinit var totpManager: TestTotpManager
    private lateinit var clipboardManager: TestClipboardManager
    private lateinit var accountManager: TestAccountManager
    private lateinit var createItem: TestCreateItem
    private lateinit var createItemAndAlias: TestCreateItemAndAlias
    private lateinit var observeVaults: TestObserveVaultsWithItemCount
    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var observeUpgradeInfo: TestObserveUpgradeInfo

    @Before
    fun setUp() {
        totpManager = TestTotpManager()
        clipboardManager = TestClipboardManager()
        accountManager = TestAccountManager()
        createItem = TestCreateItem()
        createItemAndAlias = TestCreateItemAndAlias()
        observeVaults = TestObserveVaultsWithItemCount()
        telemetryManager = TestTelemetryManager()
        snackbarDispatcher = TestSnackbarDispatcher()
        observeUpgradeInfo = TestObserveUpgradeInfo()
        instance = CreateLoginViewModel(
            accountManager = accountManager,
            createItem = createItem,
            clipboardManager = clipboardManager,
            totpManager = totpManager,
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandle = TestSavedStateHandle.create(),
            encryptionContextProvider = TestEncryptionContextProvider(),
            createItemAndAlias = createItemAndAlias,
            observeVaults = observeVaults,
            observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) },
            telemetryManager = telemetryManager,
            draftRepository = TestDraftRepository(),
            observeUpgradeInfo = observeUpgradeInfo,
            ffRepo = TestFeatureFlagsPreferenceRepository()
        )
    }

    @Test
    fun `when a create item event without title should return a BlankTitle validation error`() =
        runTest {
            val vault =
                VaultWithItemCount(Vault(ShareId("shareId"), "Share", isPrimary = false), 1, 0)
            observeVaults.sendResult(Result.success(listOf(vault)))

            instance.createItem()

            instance.createLoginUiState.test {
                assertThat(awaitItem())
                    .isEqualTo(
                        CreateLoginUiState.Initial.copy(
                            ShareUiState.Success(
                                vaultList = listOf(vault),
                                currentVault = vault
                            ),
                            BaseLoginUiState.Initial.copy(
                                validationErrors = setOf(LoginItemValidationErrors.BlankTitle),
                                totpUiState = TotpUiState.Success,
                                customFieldsState = CustomFieldsState.Disabled,
                            )
                        )
                    )
            }
        }

    @Test
    fun `given valid data when a create item event should return a success event`() = runTest {
        val item = TestItem.create(keyStoreCrypto = TestKeyStoreCrypto)
        val vault = sendInitialVault(item.shareId)

        val titleInput = "Title input"
        instance.onTitleChange(titleInput)

        val userId = UserId("user-id")
        accountManager.sendPrimaryUserId(userId)
        createItem.sendItem(Result.success(item))

        instance.createLoginUiState.test {
            instance.createItem()
            assertThat(awaitItem())
                .isEqualTo(
                    CreateLoginUiState.Initial.copy(
                        ShareUiState.Success(
                            vaultList = listOf(vault),
                            currentVault = vault
                        ),
                        BaseLoginUiState.Initial.copy(
                            loginItem = LoginItem.Empty.copy(title = titleInput),
                            isLoadingState = IsLoadingState.NotLoading,
                            hasUserEditedContent = true,
                            totpUiState = TotpUiState.Success,
                            customFieldsState = CustomFieldsState.Disabled,
                        )
                    )
                )
            assertThat(awaitItem())
                .isEqualTo(
                    CreateLoginUiState.Initial.copy(
                        ShareUiState.Success(
                            vaultList = listOf(vault),
                            currentVault = vault
                        ),
                        BaseLoginUiState.Initial.copy(
                            loginItem = LoginItem.Empty.copy(title = titleInput),
                            isLoadingState = IsLoadingState.Loading,
                            hasUserEditedContent = true,
                            totpUiState = TotpUiState.Success,
                            customFieldsState = CustomFieldsState.Disabled,
                        )
                    )
                )
            assertThat(awaitItem())
                .isEqualTo(
                    CreateLoginUiState.Initial.copy(
                        ShareUiState.Success(
                            vaultList = listOf(vault),
                            currentVault = vault
                        ),
                        BaseLoginUiState.Initial.copy(
                            loginItem = LoginItem.Empty.copy(title = titleInput),
                            isLoadingState = IsLoadingState.NotLoading,
                            hasUserEditedContent = true,
                            isItemSaved = ItemSavedState.Success(
                                item.id,
                                ItemUiModel(
                                    id = item.id,
                                    shareId = item.shareId,
                                    name = item.itemName(TestEncryptionContext),
                                    note = TestEncryptionContext.decrypt(item.note),
                                    itemType = item.itemType,
                                    createTime = item.createTime,
                                    state = 0,
                                    modificationTime = item.modificationTime,
                                    lastAutofillTime = item.lastAutofillTime.value()
                                )
                            ),
                            totpUiState = TotpUiState.Success,
                            customFieldsState = CustomFieldsState.Disabled
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
        val vault = sendInitialVault(ShareId("shareId"))
        val initialContents = setInitialContents()

        instance.createLoginUiState.test {
            assertThat(awaitItem())
                .isEqualTo(
                    CreateLoginUiState.Initial.copy(
                        shareUiState = ShareUiState.Success(
                            vaultList = listOf(
                                VaultWithItemCount(
                                    vault = Vault(
                                        vault.vault.shareId,
                                        vault.vault.name,
                                        vault.vault.color,
                                        vault.vault.icon,
                                        false
                                    ),
                                    activeItemCount = 1,
                                    trashedItemCount = 0
                                )
                            ),
                            currentVault = VaultWithItemCount(
                                vault = Vault(
                                    vault.vault.shareId,
                                    vault.vault.name,
                                    vault.vault.color,
                                    vault.vault.icon,
                                    false
                                ),
                                activeItemCount = 1,
                                trashedItemCount = 0
                            )
                        ),
                        baseLoginUiState = BaseLoginUiState.Initial.copy(
                            loginItem = LoginItem.Empty.copy(
                                title = initialContents.title!!,
                                username = initialContents.username!!,
                                password = initialContents.password!!,
                                websiteAddresses = listOf(initialContents.url!!)
                            ),
                            totpUiState = TotpUiState.Success,
                            customFieldsState = CustomFieldsState.Disabled,
                        )
                    )
                )
        }
    }

    @Test
    fun `if there is an error creating item a message is emitted`() = runTest {
        val shareId = ShareId("shareId")
        setInitialContents()
        sendInitialVault(shareId)
        createItem.sendItem(Result.failure(IllegalStateException("Test")))

        instance.createLoginUiState.test {
            skipItems(1) // Initial state
            instance.createItem()

            skipItems(1) // Loading
            val item = awaitItem()
            assertThat(createItem.hasBeenInvoked()).isTrue()

            assertThat(item.baseLoginUiState.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.baseLoginUiState.isItemSaved).isEqualTo(ItemSavedState.Unknown)

            val message = snackbarDispatcher.snackbarMessage.first().value()!!
            assertThat(message).isInstanceOf(LoginSnackbarMessages.ItemCreationError::class.java)
        }
    }

    @Test
    fun `if there is an error creating item and alias a message is emitted`() = runTest {
        val shareId = ShareId("shareId")
        setInitialContents()
        sendInitialVault(shareId)
        setTestAlias()
        createItemAndAlias.setResult(Result.failure(IllegalStateException("Test")))

        instance.createLoginUiState.test {
            skipItems(1) // Initial state
            instance.createItem()

            skipItems(1) // Loading
            val item = awaitItem()
            assertThat(createItemAndAlias.hasBeenInvoked()).isTrue()

            assertThat(item.baseLoginUiState.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.baseLoginUiState.isItemSaved).isEqualTo(ItemSavedState.Unknown)

            val message = snackbarDispatcher.snackbarMessage.first().value()!!
            assertThat(message).isInstanceOf(LoginSnackbarMessages.ItemCreationError::class.java)
        }
    }

    @Test
    fun `invalid totp emits error`() = runTest {
        val shareId = ShareId("shareId")
        setInitialContents()
        sendInitialVault(shareId)

        instance.onTotpChange("invalid://uri")
        instance.createItem()
        instance.createLoginUiState.test {
            val item = awaitItem()
            assertThat(createItem.hasBeenInvoked()).isFalse()

            assertThat(item.baseLoginUiState.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.baseLoginUiState.validationErrors).isEqualTo(
                setOf(
                    LoginItemValidationErrors.InvalidTotp
                )
            )

            val message = snackbarDispatcher.snackbarMessage.first().value()!!
            assertThat(message).isInstanceOf(LoginSnackbarMessages.InvalidTotpError::class.java)
        }
    }

    private fun setTestAlias(): AliasItem {
        val suffix = AliasSuffixUiModel(
            suffix = TestUtils.randomString(),
            signedSuffix = TestUtils.randomString(),
            isCustom = false,
            domain = TestUtils.randomString()
        )
        val mailbox = AliasMailboxUiModel(id = 1, email = TestUtils.randomString())
        val aliasItem = AliasItem(
            title = TestUtils.randomString(),
            prefix = TestUtils.randomString(),
            note = TestUtils.randomString(),
            mailboxTitle = TestUtils.randomString(),
            aliasOptions = AliasOptionsUiModel(
                suffixes = listOf(suffix),
                mailboxes = listOf(mailbox)
            ),
            selectedSuffix = suffix,
            mailboxes = listOf(SelectedAliasMailboxUiModel(mailbox, true)),
            aliasToBeCreated = TestUtils.randomString()
        )
        instance.onAliasCreated(aliasItem)
        return aliasItem
    }

    private fun setInitialContents(): InitialCreateLoginUiState {
        val initialContents = InitialCreateLoginUiState(
            title = TestUtils.randomString(),
            username = TestUtils.randomString(),
            password = TestUtils.randomString(),
            url = TestUtils.randomString(),
        )
        instance.setInitialContents(initialContents)
        accountManager.sendPrimaryUserId(UserId("UserId"))
        return initialContents
    }

    private fun sendInitialVault(shareId: ShareId): VaultWithItemCount {
        val vault = VaultWithItemCount(Vault(shareId, "Share", isPrimary = false), 1, 0)
        observeVaults.sendResult(Result.success(listOf(vault)))
        return vault
    }
}
