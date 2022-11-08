package me.proton.android.pass.ui.create.alias

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.AliasSuffix
import me.proton.pass.domain.errors.CannotCreateMoreAliasesError
import me.proton.pass.presentation.create.alias.AliasItem
import me.proton.pass.presentation.create.alias.AliasMailboxUiModel
import me.proton.pass.presentation.create.alias.AliasSnackbarMessage
import me.proton.pass.presentation.create.alias.CreateAliasViewModel
import me.proton.pass.presentation.uievents.IsLoadingState
import me.proton.pass.presentation.uievents.ItemSavedState
import me.proton.pass.test.MainDispatcherRule
import me.proton.pass.test.core.TestAccountManager
import me.proton.pass.test.core.TestSavedStateHandle
import me.proton.pass.test.data.TestAliasRepository
import me.proton.pass.test.domain.TestAliasMailbox
import me.proton.pass.test.domain.TestAliasSuffix
import me.proton.pass.test.domain.TestItem
import me.proton.pass.test.domain.TestShare
import me.proton.pass.test.domain.usecases.TestCreateAlias
import me.proton.pass.test.notification.TestSnackbarMessageRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CreateAliasViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var suffix: AliasSuffix
    private lateinit var mailbox: AliasMailbox
    private lateinit var viewModel: CreateAliasViewModel
    private lateinit var aliasRepository: TestAliasRepository
    private lateinit var createAlias: TestCreateAlias
    private lateinit var snackbarRepository: TestSnackbarMessageRepository

    @Before
    fun setUp() {
        suffix = TestAliasSuffix.create()
        mailbox = TestAliasMailbox.create()

        aliasRepository = TestAliasRepository()
        aliasRepository.setAliasOptions(
            Result.Success(
                AliasOptions(
                    suffixes = listOf(suffix),
                    mailboxes = listOf(mailbox)
                )
            )
        )

        createAlias = TestCreateAlias()
        snackbarRepository = TestSnackbarMessageRepository()
        viewModel = CreateAliasViewModel(
            accountManager = TestAccountManager().apply {
                sendPrimaryUserId(UserId("123"))
            },
            aliasRepository = aliasRepository,
            createAlias = createAlias,
            snackbarMessageRepository = snackbarRepository,
            savedStateHandle = TestSavedStateHandle.create().apply {
                set("shareId", "123")
            }
        )
    }

    @Test
    fun `is able to handle CannotCreateMoreAliases`() = runTest {
        createAlias.setResult(Result.Error(CannotCreateMoreAliasesError()))
        setupContentsForCreation()

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
        createAlias.setResult(Result.Success(TestItem.random()))
        setupContentsForCreation()

        viewModel.createAlias(TestShare.create().id)
        viewModel.aliasUiState.test {
            skipItems(2)
            val item = awaitItem()

            assertThat(item.isLoadingState).isEqualTo(IsLoadingState.NotLoading)
            assertThat(item.isItemSaved).isInstanceOf(ItemSavedState.Success::class.java)
        }
    }

    private fun setupContentsForCreation() {
        viewModel.aliasItemState.update {
            AliasItem(
                mailboxes = listOf(
                    AliasMailboxUiModel(model = mailbox, selected = false)
                )
            )
        }

        viewModel.onTitleChange("title")
        viewModel.onAliasChange("alias")
        viewModel.onSuffixChange(suffix)
        viewModel.onMailboxChange(AliasMailboxUiModel(model = mailbox, selected = false))
    }
}
