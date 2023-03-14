package proton.android.pass.featureitemcreate.impl.alias

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.fakes.usecases.TestObserveAliasOptions
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.featureitemcreate.impl.alias.CreateUpdateAliasUiState.Companion.Initial
import proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.pass.domain.AliasMailbox
import proton.pass.domain.AliasOptions
import proton.pass.domain.AliasSuffix
import proton.pass.domain.ShareId
import proton.pass.domain.Vault

internal class BaseAliasViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()


    private lateinit var snackbarMessageRepository: TestSnackbarMessageRepository
    private lateinit var observeVaults: TestObserveVaults
    private lateinit var observeAliasOptions: TestObserveAliasOptions
    private lateinit var baseAliasViewModel: BaseAliasViewModel

    @Before
    fun setUp() {
        snackbarMessageRepository = TestSnackbarMessageRepository()
        observeVaults = TestObserveVaults()
        observeAliasOptions = TestObserveAliasOptions()
        baseAliasViewModel = object : BaseAliasViewModel(
            snackbarMessageRepository,
            observeAliasOptions,
            observeVaults,
            TestSavedStateHandle.create().apply {
                set("isDraft", false)
            }
        ) {
            override fun onTitleChange(value: String) = Unit
            override fun onPrefixChange(value: String) = Unit
        }
    }

    @Test
    fun `should start with the initial state`() = runTest {
        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem()).isEqualTo(Initial)
        }
    }

    @Test
    fun `when the note has changed the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        setupVaults()
        baseAliasViewModel.onNoteChange(noteInput)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(Initial.aliasItem.copy(note = noteInput))
        }
    }

    @Test
    fun `given no alias when the suffix has changed the state should hold it`() = runTest {
        val aliasSuffix = TestAliasSuffixUiModel.create()
        setupAliasOptions(listOf(aliasSuffix.toDomain()), emptyList())
        baseAliasViewModel.onSuffixChange(aliasSuffix)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(
                    Initial.aliasItem.copy(
                        selectedSuffix = aliasSuffix,
                        aliasOptions = AliasOptionsUiModel(
                            aliasOptions = AliasOptions(
                                suffixes = listOf(aliasSuffix.toDomain()),
                                mailboxes = emptyList()
                            )
                        )
                    )
                )
        }
    }

    @Test
    fun `when the mailbox has changed the state should hold it`() = runTest {
        val aliasMailbox1 = SelectedAliasMailboxUiModel(
            model = AliasMailboxUiModel(1, "1"),
            selected = true
        )
        val aliasMailbox2 = SelectedAliasMailboxUiModel(
            model = AliasMailboxUiModel(2, "2"),
            selected = false
        )
        val mailboxList = listOf(aliasMailbox1, aliasMailbox2)
        val suffixList = listOf(TestAliasSuffixUiModel.create().toDomain())
        setupAliasOptions(suffixList, mailboxList.map { it.model.toDomain() })

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem.mailboxes).isEqualTo(mailboxList)
        }

        val enabledMailboxList = listOf(aliasMailbox1, aliasMailbox2.copy(selected = true))
        baseAliasViewModel.onMailboxesChanged(enabledMailboxList)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem.mailboxes).isEqualTo(enabledMailboxList)
        }
    }

    @Test
    fun `when there are many selected mailboxes mailboxTitle should contain an indicator`() =
        runTest {
            val firstEmail = "test"
            val secondEmail = "test2"

            // Start both as false
            val aliasMailbox1 =
                SelectedAliasMailboxUiModel(AliasMailboxUiModel(1, firstEmail), false)
            val aliasMailbox2 =
                SelectedAliasMailboxUiModel(AliasMailboxUiModel(2, secondEmail), false)
            val mailboxList = listOf(aliasMailbox1, aliasMailbox2)
            val suffixList = listOf(TestAliasSuffixUiModel.create().toDomain())
            setupAliasOptions(suffixList, mailboxList.map { it.model.toDomain() })

            // Set both to true
            baseAliasViewModel.onMailboxesChanged(
                listOf(
                    aliasMailbox1.copy(selected = true),
                    aliasMailbox2.copy(selected = true)
                )
            )

            baseAliasViewModel.aliasUiState.test {
                val item = awaitItem().aliasItem
                assertThat(item.mailboxTitle).isEqualTo(
                    """
                    $firstEmail,
                    $secondEmail
                    """.trimIndent()
                )
            }
        }

    private fun setupVaults() {
        observeVaults.sendResult(LoadingResult.Success(listOf(Vault(ShareId("ShareId"), "name"))))
    }

    private fun setupAliasOptions(
        suffixList: List<AliasSuffix> = emptyList(),
        mailboxList: List<AliasMailbox> = emptyList()
    ) {
        setupVaults()
        observeAliasOptions.sendAliasOptions(
            AliasOptions(
                suffixes = suffixList,
                mailboxes = mailboxList
            )
        )
    }
}
