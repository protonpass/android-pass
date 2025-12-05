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

package proton.android.pass.features.itemcreate.alias

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.commonpresentation.fakes.attachments.FakeAttachmentHandler
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.fakes.repositories.FakeDraftRepository
import proton.android.pass.data.fakes.usecases.FakeCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.FakeCreateAlias
import proton.android.pass.data.fakes.usecases.FakeObserveAliasOptions
import proton.android.pass.data.fakes.usecases.FakeObserveDefaultVault
import proton.android.pass.data.fakes.usecases.FakeObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.FakeObserveVaultsWithItemCount
import proton.android.pass.data.fakes.usecases.attachments.FakeLinkAttachmentsToItem
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.AliasOptions
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.alias.draftrepositories.MailboxDraftRepositoryImpl
import proton.android.pass.features.itemcreate.alias.draftrepositories.SuffixDraftRepositoryImpl
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepositoryImpl
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandlerImpl
import proton.android.pass.features.itemcreate.common.formprocessor.FakeAliasItemFormProcessor
import proton.android.pass.inappreview.fakes.FakeInAppReviewTriggerMetrics
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakeInternalSettingsRepository
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.FakeTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ShareTestFactory
import proton.android.pass.test.domain.VaultTestFactory
import proton.android.pass.totp.fakes.FakeTotpManager

class CreateAliasViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var suffix: AliasSuffixUiModel
    private lateinit var mailbox: AliasMailboxUiModel
    private lateinit var viewModel: CreateAliasViewModel
    private lateinit var observeVaults: FakeObserveVaultsWithItemCount
    private lateinit var observeAliasOptions: FakeObserveAliasOptions
    private lateinit var createAlias: FakeCreateAlias
    private lateinit var snackbarRepository: FakeSnackbarDispatcher
    private lateinit var telemetryManager: FakeTelemetryManager
    private lateinit var observeUpgradeInfo: FakeObserveUpgradeInfo
    private lateinit var canPerformPaidAction: FakeCanPerformPaidAction
    private lateinit var draftRepository: FakeDraftRepository
    private lateinit var inAppReviewTriggerMetrics: FakeInAppReviewTriggerMetrics
    private lateinit var observeShare: FakeObserveShare
    private lateinit var settingsRepository: FakeInternalSettingsRepository

    @Before
    fun setUp() {
        suffix = AliasSuffixUiModelTestFactory.create()
        mailbox = AliasMailboxUiModelTestFactory.create()

        observeVaults = FakeObserveVaultsWithItemCount()
        observeAliasOptions = FakeObserveAliasOptions()
        createAlias = FakeCreateAlias()
        snackbarRepository = FakeSnackbarDispatcher()
        telemetryManager = FakeTelemetryManager()
        draftRepository = FakeDraftRepository()
        observeUpgradeInfo = FakeObserveUpgradeInfo()
        canPerformPaidAction = FakeCanPerformPaidAction()
        inAppReviewTriggerMetrics = FakeInAppReviewTriggerMetrics()
        observeShare = FakeObserveShare()
        settingsRepository = FakeInternalSettingsRepository()
    }


    @Test
    fun `title alias sync`() = runTest {
        viewModel = createAliasViewModel()
        setupAliasOptions()
        val titleInput = "Title changed"
        viewModel.onTitleChange(titleInput)
        viewModel.createAliasUiState.test {
            assertThat(viewModel.aliasItemFormState.title).isEqualTo(titleInput)
            assertThat(viewModel.aliasItemFormState.prefix).isEqualTo("title-changed")
            assertThat(viewModel.aliasItemFormState.aliasToBeCreated).isEqualTo("title-changed${suffix.suffix}")
            cancelAndIgnoreRemainingEvents()
        }

        val newAlias = "myalias"
        viewModel.onPrefixChange(newAlias)
        viewModel.createAliasUiState.test {
            assertThat(viewModel.aliasItemFormState.title).isEqualTo(titleInput)
            assertThat(viewModel.aliasItemFormState.prefix).isEqualTo(newAlias)
            assertThat(viewModel.aliasItemFormState.aliasToBeCreated).isEqualTo("${newAlias}${suffix.suffix}")
            cancelAndIgnoreRemainingEvents()
        }

        val newTitle = "New title"
        viewModel.onTitleChange(newTitle)
        viewModel.createAliasUiState.test {
            assertThat(viewModel.aliasItemFormState.title).isEqualTo(newTitle)
            assertThat(viewModel.aliasItemFormState.prefix).isEqualTo(newAlias)
            assertThat(viewModel.aliasItemFormState.aliasToBeCreated).isEqualTo("${newAlias}${suffix.suffix}")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given no suffix when the alias has changed the state should hold it`() = runTest {
        viewModel = createAliasViewModel()
        setupVaults()
        val aliasInput = "alias-input"
        viewModel.onPrefixChange(aliasInput)

        assertThat(viewModel.aliasItemFormState)
            .isEqualTo(viewModel.aliasItemFormState.copy(prefix = aliasInput))
    }

    @Test
    fun `is able to handle CannotCreateMoreAliases`() = runTest {
        viewModel = createAliasViewModel()
        setupAliasOptions()
        createAlias.setResult(Result.failure(CannotCreateMoreAliasesError()))
        setupContentsForCreation()
        viewModel.createAliasUiState.test { awaitItem() }
        viewModel.createAlias(ShareTestFactory.random().id)

        snackbarRepository.snackbarMessage.test {
            val message = awaitItem()
            assertThat(message.isNotEmpty()).isTrue()
            message.map {
                assertThat(it).isEqualTo(AliasSnackbarMessage.CannotCreateMoreAliasesError)
            }
        }
    }

    @Test
    fun `emits success when alias is created successfully`() = runTest {
        viewModel = createAliasViewModel()
        setupAliasOptions()
        createAlias.setResult(Result.success(ItemTestFactory.random()))
        setupContentsForCreation()

        viewModel.createAliasUiState.test { awaitItem() }
        viewModel.createAlias(ShareTestFactory.random().id)
        viewModel.createAliasUiState.test {
            val item = awaitItem()

            assertThat(item.baseAliasUiState.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.baseAliasUiState.itemSavedState).isInstanceOf(ItemSavedState.Success::class.java)
        }

        val events = telemetryManager.getMemory()
        assertThat(events.size).isEqualTo(1)
        assertThat(events[0]).isEqualTo(ItemCreate(EventItemType.Alias))
    }

    @Test
    fun `emits success when draft alias is stored successfully`() = runTest {
        viewModel = createAliasViewModel(isDraft = true)
        setupAliasOptions()
        createAlias.setResult(Result.success(ItemTestFactory.random()))
        setupContentsForCreation()

        viewModel.createAliasUiState.test { awaitItem() }
        viewModel.createAlias(ShareTestFactory.random().id)
        viewModel.createAliasUiState.test {
            val item = awaitItem()

            assertThat(item.baseAliasUiState.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.baseAliasUiState.isAliasDraftSavedState)
                .isInstanceOf(AliasDraftSavedState.Success::class.java)
        }

        // No telemetry events should be emitted
        val events = telemetryManager.getMemory()
        assertThat(events).isEmpty()

        // Draft should be stored
        val draft = draftRepository.get<AliasItemFormState>(CreateAliasViewModel.KEY_DRAFT_ALIAS)
            .first()
            .value()
        assertThat(draft).isNotNull()
        assertThat(draft!!.title).isEqualTo(TEST_ALIAS_TITLE)
        assertThat(draft.prefix).isEqualTo(TEST_ALIAS_PREFIX)
    }

    @Test
    fun `spaces in title are properly formatted`() = runTest {
        viewModel = createAliasViewModel()
        setupAliasOptions()
        val titleInput = "ThiS iS a TeSt"
        viewModel.onTitleChange(titleInput)
        assertThat(viewModel.aliasItemFormState.prefix).isEqualTo("this-is-a-test")
    }

    @Test
    fun `setInitialState properly formats alias`() = runTest {
        viewModel = createAliasViewModel(title = "ThiS.iS_a TeSt")
        setupAliasOptions()
        assertThat(viewModel.aliasItemFormState.prefix).isEqualTo("this.is_a-test")
    }

    @Test
    fun `onPrefixChange does not allow emojis`() = runTest {
        viewModel = createAliasViewModel()
        setupAliasOptions()
        val firstPrefix = "someprefix"
        viewModel.onPrefixChange(firstPrefix)
        val titleInput = "$firstPrefix😀"
        viewModel.onPrefixChange(titleInput)
        assertThat(viewModel.aliasItemFormState.prefix).isEqualTo(firstPrefix)
    }

    private fun createAliasViewModel(title: String? = null, isDraft: Boolean = false) = CreateAliasViewModel(
        accountManager = FakeAccountManager().apply {
            sendPrimaryUserId(UserId("123"))
        },
        observeAliasOptions = observeAliasOptions,
        observeVaults = observeVaults,
        createAlias = createAlias,
        snackbarDispatcher = snackbarRepository,
        savedStateHandleProvider = FakeSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = "123"
            title?.let {
                get()[AliasOptionalNavArgId.Title.key] = title
            }
        },
        telemetryManager = telemetryManager,
        observeUpgradeInfo = observeUpgradeInfo,
        draftRepository = draftRepository,
        inAppReviewTriggerMetrics = FakeInAppReviewTriggerMetrics(),
        encryptionContextProvider = FakeEncryptionContextProvider(),
        observeDefaultVault = FakeObserveDefaultVault(),
        linkAttachmentsToItem = FakeLinkAttachmentsToItem(),
        attachmentsHandler = FakeAttachmentHandler(),
        userPreferencesRepository = FakePreferenceRepository(),
        mailboxDraftRepository = MailboxDraftRepositoryImpl(),
        suffixDraftRepository = SuffixDraftRepositoryImpl(),
        customFieldHandler = CustomFieldHandlerImpl(FakeTotpManager(), FakeEncryptionContextProvider()),
        customFieldDraftRepository = CustomFieldDraftRepositoryImpl(),
        canPerformPaidAction = FakeCanPerformPaidAction(),
        aliasItemFormProcessor = FakeAliasItemFormProcessor(),
        clipboardManager = FakeClipboardManager(),
        observeShare = observeShare,
        settingsRepository = settingsRepository
    ).apply {
        setDraftStatus(isDraft)
    }

    private fun setupContentsForCreation() {
        viewModel.onTitleChange(TEST_ALIAS_TITLE)
        viewModel.onPrefixChange(TEST_ALIAS_PREFIX)
    }

    private fun setupVaults() {
        observeVaults.sendResult(
            Result.success(
                listOf(
                    VaultWithItemCount(
                        vault = VaultTestFactory.create(shareId = ShareId("ShareId"), name = "name"),
                        activeItemCount = 1,
                        trashedItemCount = 0
                    )
                )
            )
        )
    }

    private fun setupAliasOptions() {
        setupVaults()
        observeAliasOptions.sendAliasOptions(
            AliasOptions(
                suffixes = listOf(suffix.toDomain()),
                mailboxes = listOf(mailbox.toDomain())
            )
        )
    }

    companion object {
        private val SHARE_ID = ShareId("123")
        private const val TEST_ALIAS_TITLE = "title"
        private const val TEST_ALIAS_PREFIX = "prefix"
    }
}
