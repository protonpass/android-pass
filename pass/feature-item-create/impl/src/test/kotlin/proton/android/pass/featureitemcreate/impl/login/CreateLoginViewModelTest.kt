/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.featureitemcreate.impl.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.clipboard.fakes.TestClipboardManager
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonui.fakes.TestSavedStateHandleProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.errors.EmailNotValidatedError
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestCreateItem
import proton.android.pass.data.fakes.usecases.TestCreateItemAndAlias
import proton.android.pass.data.fakes.usecases.TestObserveCurrentUser
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.alias.AliasMailboxUiModel
import proton.android.pass.featureitemcreate.impl.alias.AliasOptionsUiModel
import proton.android.pass.featureitemcreate.impl.alias.AliasSuffixUiModel
import proton.android.pass.featureitemcreate.impl.alias.SelectedAliasMailboxUiModel
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.preferences.TestIncItemCreatedCount
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestUtils
import proton.android.pass.test.domain.TestUser
import proton.android.pass.totp.fakes.TestTotpManager
import proton.pass.domain.HiddenState
import proton.pass.domain.ItemState
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
            savedStateHandle = TestSavedStateHandleProvider(),
            encryptionContextProvider = TestEncryptionContextProvider(),
            createItemAndAlias = createItemAndAlias,
            observeVaults = observeVaults,
            observeCurrentUser = TestObserveCurrentUser().apply { sendUser(TestUser.create()) },
            telemetryManager = telemetryManager,
            draftRepository = TestDraftRepository(),
            observeUpgradeInfo = observeUpgradeInfo,
            canPerformPaidAction = TestCanPerformPaidAction().apply { setResult(true) },
            incItemCreatedCount = TestIncItemCreatedCount()
        )
    }

    @Test
    fun `when a create item event without title should return a BlankTitle validation error`() =
        runTest {
            val vault =
                VaultWithItemCount(Vault(ShareId("shareId"), "Share", isPrimary = false), 1, 0)
            observeVaults.sendResult(Result.success(listOf(vault)))

            val state = CreateLoginUiState.create(
                password = HiddenState.Empty(TestEncryptionContext.encrypt("")),
                primaryTotp = HiddenState.Revealed(TestEncryptionContext.encrypt(""), "")
            )
            instance.createItem()

            instance.createLoginUiState.test {
                assertThat(awaitItem())
                    .isEqualTo(
                        state.copy(
                            ShareUiState.Success(
                                vaultList = listOf(vault),
                                currentVault = vault
                            ),
                            state.baseLoginUiState.copy(
                                validationErrors = persistentSetOf(LoginItemValidationErrors.BlankTitle),
                                totpUiState = TotpUiState.Success,
                                customFieldsState = CustomFieldsState.Disabled,
                            )
                        )
                    )
            }
        }

    @Test
    fun `given valid data when a create item event should return a success event`() = runTest {
        val item = TestObserveItems.createLogin()
        val vault = sendInitialVault(item.shareId)
        val baseState = CreateLoginUiState.create(
            password = HiddenState.Empty(TestEncryptionContext.encrypt("")),
            primaryTotp = HiddenState.Empty(TestEncryptionContext.encrypt(""))
        )
        val titleInput = "Title input"
        instance.onTitleChange(titleInput)

        val userId = UserId("user-id")
        accountManager.sendPrimaryUserId(userId)
        createItem.sendItem(Result.success(item))

        instance.createLoginUiState.test {
            instance.createItem()
            val firstItem = awaitItem()
            val firstExpected = baseState.copy(
                shareUiState = ShareUiState.Success(
                    vaultList = listOf(vault),
                    currentVault = vault
                ),
                baseLoginUiState = baseState.baseLoginUiState.copy(
                    contents = baseState.baseLoginUiState.contents.copy(title = titleInput),
                    isLoadingState = IsLoadingState.NotLoading,
                    hasUserEditedContent = true,
                    totpUiState = TotpUiState.Success,
                    customFieldsState = CustomFieldsState.Disabled,
                )
            )
            assertThat(firstItem).isEqualTo(firstExpected)
            val secondItem = awaitItem()
            val secondExpected = firstExpected.copy(
                baseLoginUiState = firstExpected.baseLoginUiState.copy(
                    isLoadingState = IsLoadingState.Loading,
                    contents = firstExpected.baseLoginUiState.contents.copy(
                        password = HiddenState.Empty(TestEncryptionContext.encrypt("")),
                        primaryTotp = HiddenState.Revealed(TestEncryptionContext.encrypt(""), "")
                    )
                )
            )
            assertThat(secondItem).isEqualTo(secondExpected)
            val thirdItem = awaitItem()
            val thirdExpected = secondExpected.copy(
                baseLoginUiState = secondExpected.baseLoginUiState.copy(
                    isLoadingState = IsLoadingState.NotLoading,
                    isItemSaved = ItemSavedState.Success(
                        item.id,
                        ItemUiModel(
                            id = item.id,
                            shareId = item.shareId,
                            contents = item.toItemContents(TestEncryptionContext),
                            createTime = item.createTime,
                            state = ItemState.Active.value,
                            modificationTime = item.modificationTime,
                            lastAutofillTime = item.lastAutofillTime.value()
                        )
                    )
                )
            )
            assertThat(thirdItem).isEqualTo(thirdExpected)
        }

        val memory = telemetryManager.getMemory()
        assertThat(memory.size).isEqualTo(1)
        assertThat(memory[0]).isEqualTo(ItemCreate(EventItemType.Login))
    }

    @Test
    fun `setting initial data emits the proper contents`() = runTest {
        val vault = sendInitialVault(ShareId("shareId"))
        val initialContents = setInitialContents()
        val state = CreateLoginUiState.create(
            password = HiddenState.Empty(TestEncryptionContext.encrypt("")),
            primaryTotp = HiddenState.Empty(TestEncryptionContext.encrypt(""))
        )
        instance.createLoginUiState.test {
            assertThat(awaitItem())
                .isEqualTo(
                    state.copy(
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
                        baseLoginUiState = state.baseLoginUiState.copy(
                            contents = state.baseLoginUiState.contents.copy(
                                title = initialContents.title!!,
                                username = initialContents.username!!,
                                password = HiddenState.Concealed(
                                    TestEncryptionContext.encrypt(initialContents.password!!)
                                ),
                                urls = listOf(initialContents.url!!)
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
    fun `if email is not validated when creating item with alias a message is emitted`() = runTest {
        val shareId = ShareId("shareId")
        setInitialContents()
        sendInitialVault(shareId)
        setTestAlias()
        createItemAndAlias.setResult(Result.failure(EmailNotValidatedError()))

        instance.createLoginUiState.test {
            skipItems(1) // Initial state
            instance.createItem()

            skipItems(1) // Loading
            val item = awaitItem()
            assertThat(createItemAndAlias.hasBeenInvoked()).isTrue()

            assertThat(item.baseLoginUiState.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.baseLoginUiState.isItemSaved).isEqualTo(ItemSavedState.Unknown)

            val message = snackbarDispatcher.snackbarMessage.first().value()!!
            assertThat(message).isInstanceOf(LoginSnackbarMessages.EmailNotValidated::class.java)
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
