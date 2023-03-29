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
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.fakes.repositories.TestDraftRepository
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestObserveAliasOptions
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.navigation.api.AliasOptionalNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.fakes.TestTelemetryManager
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestAccountManager
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
    }


    @Test
    fun `title alias sync`() = runTest {
        viewModel = createAliasViewModel()
        setupAliasOptions()
        val titleInput = "Title changed"
        viewModel.onSuffixChange(suffix)
        viewModel.onTitleChange(titleInput)

        viewModel.aliasUiState.test {
            val item = awaitItem()
            assertThat(item.aliasItem.title).isEqualTo(titleInput)
            assertThat(item.aliasItem.prefix).isEqualTo("title-changed")
            assertThat(item.aliasItem.aliasToBeCreated).isEqualTo("title-changed${suffix.suffix}")

            cancelAndIgnoreRemainingEvents()
        }

        val newAlias = "myalias"
        viewModel.onPrefixChange(newAlias)

        viewModel.aliasUiState.test {
            val item = awaitItem()
            assertThat(item.aliasItem.title).isEqualTo(titleInput)
            assertThat(item.aliasItem.prefix).isEqualTo(newAlias)
            assertThat(item.aliasItem.aliasToBeCreated).isEqualTo("${newAlias}${suffix.suffix}")

            cancelAndIgnoreRemainingEvents()
        }

        val newTitle = "New title"
        viewModel.onTitleChange(newTitle)

        viewModel.aliasUiState.test {
            val item = awaitItem()
            assertThat(item.aliasItem.title).isEqualTo(newTitle)
            assertThat(item.aliasItem.prefix).isEqualTo(newAlias)
            assertThat(item.aliasItem.aliasToBeCreated).isEqualTo("${newAlias}${suffix.suffix}")

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given no suffix when the alias has changed the state should hold it`() = runTest {
        viewModel = createAliasViewModel()
        setupVaults()
        val aliasInput = "aliasInput"
        viewModel.onPrefixChange(aliasInput)

        viewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(CreateUpdateAliasUiState.Initial.aliasItem.copy(prefix = aliasInput))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `is able to handle CannotCreateMoreAliases`() = runTest {
        viewModel = createAliasViewModel()
        setupAliasOptions()
        createAlias.setResult(LoadingResult.Error(CannotCreateMoreAliasesError()))
        setupContentsForCreation()
        viewModel.aliasUiState.test { awaitItem() }
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
        createAlias.setResult(LoadingResult.Success(TestItem.random()))
        setupContentsForCreation()

        viewModel.aliasUiState.test { awaitItem() }
        viewModel.createAlias(TestShare.create().id)
        viewModel.aliasUiState.test {
            skipItems(1)
            val item = awaitItem()

            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.isAliasSavedState).isInstanceOf(AliasSavedState.Success::class.java)
        }

        val events = telemetryManager.getMemory()
        assertThat(events.size).isEqualTo(1)
        assertThat(events[0]).isEqualTo(ItemCreate(EventItemType.Alias))
    }

    @Test
    fun `emits success when draft alias is stored successfully`() = runTest {
        viewModel = createAliasViewModel(isDraft = true)
        setupAliasOptions()
        createAlias.setResult(LoadingResult.Success(TestItem.random()))
        setupContentsForCreation()

        viewModel.aliasUiState.test { awaitItem() }
        viewModel.createAlias(TestShare.create().id)
        viewModel.aliasUiState.test {
            val item = awaitItem()

            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.isAliasDraftSavedState).isInstanceOf(AliasDraftSavedState.Success::class.java)
        }

        // No telemetry events should be emitted
        val events = telemetryManager.getMemory()
        assertThat(events).isEmpty()

        // Draft should be stored
        val draft = draftRepository.get<AliasItem>(KEY_DRAFT_ALIAS).first().value()
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
        viewModel.aliasUiState.test {
            val item = awaitItem()
            assertThat(item.aliasItem.prefix).isEqualTo("this-is-a-test")
        }
    }

    @Test
    fun `setInitialState properly formats alias`() = runTest {
        viewModel = createAliasViewModel(title = "ThiS.iS_a TeSt")
        setupAliasOptions()
        viewModel.aliasUiState.test {
            val item = awaitItem()
            assertThat(item.aliasItem.prefix).isEqualTo("this.is_a-test")
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
            LoadingResult.Success(
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
