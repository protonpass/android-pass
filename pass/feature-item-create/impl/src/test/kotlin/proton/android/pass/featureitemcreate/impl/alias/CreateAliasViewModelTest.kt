package proton.android.pass.featureitemcreate.impl.alias

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.fakes.usecases.TestCreateAlias
import proton.android.pass.data.fakes.usecases.TestObserveAliasOptions
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestAccountManager
import proton.android.pass.test.TestSavedStateHandle
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestShare
import proton.pass.domain.AliasOptions
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

class CreateAliasViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var suffix: AliasSuffixUiModel
    private lateinit var mailbox: AliasMailboxUiModel
    private lateinit var viewModel: CreateAliasViewModel
    private lateinit var observeVaults: TestObserveVaults
    private lateinit var observeAliasOptions: TestObserveAliasOptions
    private lateinit var createAlias: TestCreateAlias
    private lateinit var snackbarRepository: TestSnackbarMessageRepository

    @Before
    fun setUp() {
        suffix = TestAliasSuffixUiModel.create()
        mailbox = TestAliasMailboxUiModel.create()

        observeVaults = TestObserveVaults()
        observeAliasOptions = TestObserveAliasOptions()
        createAlias = TestCreateAlias()
        snackbarRepository = TestSnackbarMessageRepository()
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
            snackbarMessageRepository = snackbarRepository,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set("shareId", "123")
                set("isDraft", isDraft)
                title?.let {
                    set("aliasTitle", title)
                }
            }
        )

    private fun setupContentsForCreation() {
        viewModel.aliasItemState.update {
            AliasItem(
                mailboxes = listOf(
                    SelectedAliasMailboxUiModel(model = mailbox, selected = false)
                )
            )
        }

        viewModel.onTitleChange("title")
        viewModel.onPrefixChange("alias")
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
        observeVaults.sendResult(LoadingResult.Success(listOf(Vault(ShareId("ShareId"), "name"))))
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
}
