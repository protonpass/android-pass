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

package proton.android.pass.featureitemcreate.impl.alias

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.account.fakes.TestAccountManager
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.usecases.TestCanPerformPaidAction
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestObserveAliasOptions
import proton.android.pass.data.fakes.usecases.TestObserveUpgradeInfo
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestShare
import proton.pass.domain.AliasOptions
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

class CreateAliasViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var suffix: AliasSuffixUiModel
    private lateinit var mailbox: AliasMailboxUiModel
    private lateinit var viewModel: CreateAliasViewModel
    private lateinit var observeVaults: TestObserveVaultsWithItemCount
    private lateinit var observeAliasOptions: TestObserveAliasOptions
    private lateinit var createAlias: TestCreateAlias
    private lateinit var snackbarRepository: TestSnackbarDispatcher
    private lateinit var telemetryManager: TestTelemetryManager
    private lateinit var observeUpgradeInfo: TestObserveUpgradeInfo
    private lateinit var canPerformPaidAction: TestCanPerformPaidAction
    private lateinit var draftRepository: TestDraftRepository

    @Before
    fun setUp() {
        suffix = TestAliasSuffixUiModel.create()
        mailbox = TestAliasMailboxUiModel.create()

        observeVaults = TestObserveVaultsWithItemCount()
        observeAliasOptions = TestObserveAliasOptions()
        createAlias = TestCreateAlias()
        snackbarRepository = TestSnackbarDispatcher()
        telemetryManager = TestTelemetryManager()
        draftRepository = TestDraftRepository()
        observeUpgradeInfo = TestObserveUpgradeInfo()
        canPerformPaidAction = TestCanPerformPaidAction()
    }


    @Test
    fun `title alias sync`() = runTest {
        viewModel = createAliasViewModel()
        setupAliasOptions()
        val titleInput = "Title changed"

        viewModel.onSuffixChange(suffix)
        viewModel.onTitleChange(titleInput)

        viewModel.createAliasUiState.test {
            val item = awaitItem()
            assertThat(item.baseAliasUiState.aliasItem.title).isEqualTo(titleInput)
            assertThat(item.baseAliasUiState.aliasItem.prefix).isEqualTo("title-changed")
            assertThat(item.baseAliasUiState.aliasItem.aliasToBeCreated).isEqualTo("title-changed${suffix.suffix}")

            cancelAndIgnoreRemainingEvents()
        }

        val newAlias = "myalias"
        viewModel.onPrefixChange(newAlias)

        viewModel.createAliasUiState.test {
            val item = awaitItem()
            assertThat(item.baseAliasUiState.aliasItem.title).isEqualTo(titleInput)
            assertThat(item.baseAliasUiState.aliasItem.prefix).isEqualTo(newAlias)
            assertThat(item.baseAliasUiState.aliasItem.aliasToBeCreated).isEqualTo("${newAlias}${suffix.suffix}")

            cancelAndIgnoreRemainingEvents()
        }

        val newTitle = "New title"
        viewModel.onTitleChange(newTitle)

        viewModel.createAliasUiState.test {
            val item = awaitItem()
            assertThat(item.baseAliasUiState.aliasItem.title).isEqualTo(newTitle)
            assertThat(item.baseAliasUiState.aliasItem.prefix).isEqualTo(newAlias)
            assertThat(item.baseAliasUiState.aliasItem.aliasToBeCreated).isEqualTo("${newAlias}${suffix.suffix}")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given no suffix when the alias has changed the state should hold it`() = runTest {
        viewModel = createAliasViewModel()
        setupVaults()
        val aliasInput = "alias-input"
        viewModel.onPrefixChange(aliasInput)

        viewModel.createAliasUiState.test {
            assertThat(awaitItem().baseAliasUiState.aliasItem)
                .isEqualTo(BaseAliasUiState.Initial.aliasItem.copy(prefix = aliasInput))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `is able to handle CannotCreateMoreAliases`() = runTest {
        viewModel = createAliasViewModel()
        setupAliasOptions()
        createAlias.setResult(Result.failure(CannotCreateMoreAliasesError()))
        setupContentsForCreation()
        viewModel.createAliasUiState.test { awaitItem() }
        viewModel.createAlias(TestShare.create().id)

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
        createAlias.setResult(Result.success(TestItem.random()))
        setupContentsForCreation()

        viewModel.createAliasUiState.test { awaitItem() }
        viewModel.createAlias(TestShare.create().id)
        viewModel.createAliasUiState.test {
            skipItems(1)
            val item = awaitItem()

            assertThat(item.baseAliasUiState.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.baseAliasUiState.isAliasSavedState).isInstanceOf(AliasSavedState.Success::class.java)
        }

        val events = telemetryManager.getMemory()
        assertThat(events.size).isEqualTo(1)
        assertThat(events[0]).isEqualTo(ItemCreate(EventItemType.Alias))
    }

    @Test
    fun `emits success when draft alias is stored successfully`() = runTest {
        viewModel = createAliasViewModel(isDraft = true)
        setupAliasOptions()
        createAlias.setResult(Result.success(TestItem.random()))
        setupContentsForCreation()

        viewModel.createAliasUiState.test { awaitItem() }
        viewModel.createAlias(TestShare.create().id)
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
        val draft = draftRepository.get<AliasItem>(CreateAliasViewModel.KEY_DRAFT_ALIAS)
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
        viewModel.createAliasUiState.test {
            val item = awaitItem()
            assertThat(item.baseAliasUiState.aliasItem.prefix).isEqualTo("this-is-a-test")
        }
    }

    @Test
    fun `setInitialState properly formats alias`() = runTest {
        viewModel = createAliasViewModel(title = "ThiS.iS_a TeSt")
        setupAliasOptions()
        viewModel.createAliasUiState.test {
            val item = awaitItem()
            assertThat(item.baseAliasUiState.aliasItem.prefix).isEqualTo("this.is_a-test")
        }
    }

    @Test
    fun `onPrefixChange does not allow emojis`() = runTest {
        viewModel = createAliasViewModel()
        setupAliasOptions()
        val firstPrefix = "someprefix"
        viewModel.onPrefixChange(firstPrefix)
        val titleInput = "$firstPrefix😀"
        viewModel.onPrefixChange(titleInput)
        viewModel.createAliasUiState.test {
            val item = awaitItem()
            assertThat(item.baseAliasUiState.aliasItem.prefix).isEqualTo(firstPrefix)
        }
    }

    private fun createAliasViewModel(title: String? = null, isDraft: Boolean = false) =
        CreateAliasViewModel(
            accountManager = TestAccountManager().apply {
                sendPrimaryUserId(UserId("123"))
            },
            observeAliasOptions = observeAliasOptions,
            observeVaults = observeVaults,
            createAlias = createAlias,
            snackbarDispatcher = snackbarRepository,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set(CommonNavArgId.ShareId.key, "123")
                title?.let {
                    set(AliasOptionalNavArgId.Title.key, title)
                }
            },
            telemetryManager = telemetryManager,
            observeUpgradeInfo = observeUpgradeInfo,
            canPerformPaidAction = canPerformPaidAction,
            draftRepository = draftRepository
        ).apply {
            setDraftStatus(isDraft)
        }

    private fun setupContentsForCreation() {
        viewModel.aliasItemState.update {
            AliasItem(
                mailboxes = listOf(
                    SelectedAliasMailboxUiModel(model = mailbox, selected = false)
                )
            )
        }

        viewModel.onTitleChange(TEST_ALIAS_TITLE)
        viewModel.onPrefixChange(TEST_ALIAS_PREFIX)
        viewModel.onSuffixChange(suffix)
        viewModel.onMailboxesChanged(
            listOf(
                SelectedAliasMailboxUiModel(
                    model = mailbox,
                    selected = true
                )
            )
        )
    }

    private fun setupVaults() {
        observeVaults.sendResult(
            Result.success(
                listOf(
                    VaultWithItemCount(
                        vault = Vault(ShareId("ShareId"), "name", isPrimary = false),
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
        private const val TEST_ALIAS_TITLE = "title"
        private const val TEST_ALIAS_PREFIX = "prefix"
    }
}
