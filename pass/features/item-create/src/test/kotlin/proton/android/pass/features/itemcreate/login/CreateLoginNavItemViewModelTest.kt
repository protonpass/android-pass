/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemcreate.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.commonpresentation.fakes.attachments.FakeAttachmentHandler
import proton.android.pass.commonrust.fakes.FakeEmailValidator
import proton.android.pass.commonrust.fakes.passwords.strengths.FakePasswordStrengthCalculator
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.errors.EmailNotValidatedError
import proton.android.pass.data.fakes.repositories.FakeDraftRepository
import proton.android.pass.data.fakes.usecases.FakeGetItemById
import proton.android.pass.data.fakes.usecases.FakeCreateItem
import proton.android.pass.data.fakes.usecases.FakeCreateLoginAndAlias
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.fakes.usecases.FakeObserveDefaultVault
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.attachments.FakeLinkAttachmentsToItem
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.data.fakes.usecases.tooltips.FakeDisableTooltip
import proton.android.pass.data.fakes.usecases.tooltips.FakeObserveTooltipEnabled
import proton.android.pass.data.fakes.work.FakeWorkerLauncher
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.toItemContents
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.MFACreated
import proton.android.pass.features.itemcreate.alias.AliasItemFormState
import proton.android.pass.features.itemcreate.alias.AliasMailboxUiModel
import proton.android.pass.features.itemcreate.alias.AliasOptionsUiModel
import proton.android.pass.features.itemcreate.alias.AliasSuffixUiModel
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepositoryImpl
import proton.android.pass.features.itemcreate.common.LoginItemValidationError
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandlerImpl
import proton.android.pass.features.itemcreate.common.formprocessor.FakeLoginItemFormProcessor
import proton.android.pass.features.itemcreate.common.formprocessor.FormProcessingResult
import proton.android.pass.inappreview.fakes.FakeInAppReviewTriggerMetrics
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.passkeys.fakes.FakeGeneratePasskey
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.StringTestFactory
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.UserTestFactory
import proton.android.pass.test.domain.VaultTestFactory
import proton.android.pass.totp.fakes.FakeTotpManager

internal class CreateLoginNavItemViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var instance: CreateLoginViewModel
    private lateinit var totpManager: FakeTotpManager
    private lateinit var clipboardManager: FakeClipboardManager
    private lateinit var accountManager: FakeAccountManager
    private lateinit var createItem: FakeCreateItem
    private lateinit var createItemAndAlias: FakeCreateLoginAndAlias
    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var observeUpgradeInfo: FakeObserveUpgradeInfo
    private lateinit var encryptionContextProvider: EncryptionContextProvider
    private lateinit var loginItemFormProcessor: FakeLoginItemFormProcessor
    private lateinit var observeShare: FakeObserveShare
    private lateinit var settingsRepository: FakeInternalSettingsRepository

    @Before
    fun setUp() {
        totpManager = FakeTotpManager()
        clipboardManager = FakeClipboardManager()
        accountManager = FakeAccountManager()
        createItem = FakeCreateItem()
        createItemAndAlias = FakeCreateLoginAndAlias()
        observeVaults = FakeObserveVaultsWithItemCount()
        telemetryManager = FakeTelemetryManager()
        snackbarDispatcher = FakeSnackbarDispatcher()
        observeUpgradeInfo = FakeObserveUpgradeInfo()
        encryptionContextProvider = FakeEncryptionContextProvider()
        loginItemFormProcessor = FakeLoginItemFormProcessor()
        observeShare = FakeObserveShare()
        settingsRepository = FakeInternalSettingsRepository()
        instance = CreateLoginViewModel(
            accountManager = accountManager,
            createItem = createItem,
            clipboardManager = clipboardManager,
            totpManager = totpManager,
            snackbarDispatcher = snackbarDispatcher,
            savedStateHandleProvider = FakeSavedStateHandleProvider(),
            encryptionContextProvider = encryptionContextProvider,
            passwordStrengthCalculator = FakePasswordStrengthCalculator(),
            createLoginAndAlias = createItemAndAlias,
            observeVaults = observeVaults,
            observeCurrentUser = FakeObserveCurrentUser().apply { sendUser(UserTestFactory.create()) },
            telemetryManager = telemetryManager,
            draftRepository = FakeDraftRepository(),
            observeUpgradeInfo = observeUpgradeInfo,
            inAppReviewTriggerMetrics = FakeInAppReviewTriggerMetrics(),
            observeDefaultVault = FakeObserveDefaultVault(),
            generatePasskey = FakeGeneratePasskey(),
            emailValidator = FakeEmailValidator(),
            observeTooltipEnabled = FakeObserveTooltipEnabled(),
            disableTooltip = FakeDisableTooltip(),
            workerLauncher = FakeWorkerLauncher(),
            userPreferencesRepository = FakePreferenceRepository(),
            linkAttachmentsToItem = FakeLinkAttachmentsToItem(),
            attachmentsHandler = FakeAttachmentHandler(),
            customFieldDraftRepository = CustomFieldDraftRepositoryImpl(),
            customFieldHandler = CustomFieldHandlerImpl(FakeTotpManager(), encryptionContextProvider),
            loginItemFormProcessor = loginItemFormProcessor,
            getItemById = FakeGetItemById(),
            observeShare = observeShare,
            settingsRepository = settingsRepository
        )
    }

    @Test
    fun `when a create item event without title should return a BlankTitle validation error`() = runTest {
        val vault = VaultTestFactory.create(shareId = ShareId("shareId"), name = "Share")
        val vaultWithItemCount = VaultWithItemCount(vault, 1, 0)
        loginItemFormProcessor.setResult(
            FormProcessingResult.Error(setOf(CommonFieldValidationError.BlankTitle))
        )
        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount)))

        instance.createItem()

        instance.createLoginUiState.test {
            assertThat(awaitItem())
                .isEqualTo(
                    CreateLoginUiState.Initial.copy(
                        ShareUiState.Success(
                            vaultList = listOf(vaultWithItemCount),
                            currentVault = vaultWithItemCount
                        ),
                        BaseLoginUiState.Initial.copy(
                            validationErrors = persistentSetOf(CommonFieldValidationError.BlankTitle),
                            totpUiState = TotpUiState.Success
                        )
                    )
                )
        }
    }

    @Test
    fun `given valid data when a create item event should return a success event`() = runTest {
        val item = ItemTestFactory.createLogin(primaryTotp = "secret")
        val vault = sendInitialVault(item.shareId)
        val baseState = CreateLoginUiState.Initial

        val titleInput = "Title input"
        instance.onTitleChange(titleInput)
        accountManager.sendPrimaryUserId(UserId("user-id"))
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
                    isLoadingState = IsLoadingState.NotLoading,
                    hasUserEditedContent = true,
                    totpUiState = TotpUiState.Success
                )
            )
            assertThat(firstItem).isEqualTo(firstExpected)
            val secondItem = awaitItem()
            val secondExpected = firstItem.copy(
                baseLoginUiState = firstItem.baseLoginUiState.copy(
                    isLoadingState = IsLoadingState.NotLoading,
                    isItemSaved = ItemSavedState.Success(
                        item.id,
                        ItemUiModel(
                            id = item.id,
                            userId = UserId("user-id"),
                            shareId = item.shareId,
                            contents = item.toItemContents { FakeEncryptionContext.decrypt(it) },
                            createTime = item.createTime,
                            state = ItemState.Active.value,
                            modificationTime = item.modificationTime,
                            lastAutofillTime = item.lastAutofillTime.value(),
                            isPinned = false,
                            pinTime = item.pinTime.value(),
                            category = ItemCategory.Login,
                            revision = item.revision,
                            shareCount = item.shareCount,
                            shareType = item.shareType
                        )
                    )
                )
            )
            assertThat(secondItem).isEqualTo(secondExpected)
        }

        val memory = telemetryManager.getMemory()
        assertThat(memory.size).isEqualTo(2)
        assertThat(memory[0]).isEqualTo(ItemCreate(EventItemType.Login))
        assertThat(memory[1]).isEqualTo(MFACreated)
    }

    @Test
    fun `if there is an error creating item a message is emitted`() = runTest {
        val shareId = ShareId("shareId")
        setInitialContents()
        sendInitialVault(shareId)
        createItem.sendItem(Result.failure(IllegalStateException("Test")))

        instance.createLoginUiState.test {
            instance.createItem()

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
            instance.createItem()

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
            instance.createItem()

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
        loginItemFormProcessor.setResult(
            FormProcessingResult.Error(setOf(LoginItemValidationError.InvalidPrimaryTotp))
        )
        val uri = "invalid://uri"
        totpManager.setSanitisedEditResult(Result.success(uri))
        totpManager.addSanitisedSaveResult(Result.failure(RuntimeException()))

        instance.onTotpChange(uri)
        instance.createItem()
        instance.createLoginUiState.test {
            val item = awaitItem()
            assertThat(createItem.hasBeenInvoked()).isFalse()

            assertThat(item.baseLoginUiState.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.baseLoginUiState.validationErrors).isEqualTo(
                setOf(LoginItemValidationError.InvalidPrimaryTotp)
            )
        }
    }

    private fun setTestAlias(): AliasItemFormState {
        val suffix = AliasSuffixUiModel(
            suffix = StringTestFactory.randomString(),
            signedSuffix = StringTestFactory.randomString(),
            isCustom = false,
            isPremium = false,
            domain = StringTestFactory.randomString()
        )
        val mailbox = AliasMailboxUiModel(id = 1, email = StringTestFactory.randomString())
        val aliasItemFormState = AliasItemFormState(
            title = StringTestFactory.randomString(),
            prefix = StringTestFactory.randomString(),
            note = StringTestFactory.randomString(),
            aliasOptions = AliasOptionsUiModel(
                suffixes = listOf(suffix),
                mailboxes = listOf(mailbox)
            ),
            selectedSuffix = suffix,
            selectedMailboxes = setOf(mailbox),
            aliasToBeCreated = StringTestFactory.randomString(),
            customFields = emptyList()
        )
        instance.onAliasCreated(aliasItemFormState)
        return aliasItemFormState
    }

    private fun setInitialContents(): InitialCreateLoginUiState {
        val initialContents = InitialCreateLoginUiState(
            title = StringTestFactory.randomString(),
            username = StringTestFactory.randomString(),
            password = StringTestFactory.randomString(),
            url = StringTestFactory.randomString()
        )
        instance.setInitialContents(initialContents)
        accountManager.sendPrimaryUserId(UserId("UserId"))
        return initialContents
    }

    private fun sendInitialVault(shareId: ShareId): VaultWithItemCount {
        val vault = VaultTestFactory.create(shareId = shareId, name = "Share")
        val vaultWithItemCount = VaultWithItemCount(vault, 1, 0)
        observeVaults.sendResult(Result.success(listOf(vaultWithItemCount)))
        return vaultWithItemCount
    }
}
