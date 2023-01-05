package me.proton.android.pass.featurecreateitem.impl.alias

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import me.proton.android.pass.featurecreateitem.impl.alias.CreateUpdateAliasUiState.Companion.Initial
import me.proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import me.proton.pass.test.MainDispatcherRule
import me.proton.pass.test.TestSavedStateHandle
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class BaseAliasViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var baseAliasViewModel: BaseAliasViewModel

    @Before
    fun setUp() {
        baseAliasViewModel = object : BaseAliasViewModel(
            TestSnackbarMessageRepository(),
            TestSavedStateHandle.create().apply {
                set("isDraft", false)
            }
        ) {
            override fun onTitleChange(value: String) = Unit
            override fun onAliasChange(value: String) = Unit
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
        baseAliasViewModel.onNoteChange(noteInput)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(Initial.aliasItem.copy(note = noteInput))
        }
    }

    @Test
    fun `given no alias when the suffix has changed the state should hold it`() = runTest {
        val aliasSuffix = TestAliasSuffixUiModel.create()
        baseAliasViewModel.onSuffixChange(aliasSuffix)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(Initial.aliasItem.copy(selectedSuffix = aliasSuffix))
        }
    }

    @Test
    fun `when the mailbox has changed the state should hold it`() = runTest {
        val aliasMailbox1 = SelectedAliasMailboxUiModel(AliasMailboxUiModel(1, "1"), true)
        val aliasMailbox2 = SelectedAliasMailboxUiModel(AliasMailboxUiModel(2, "2"), true)
        baseAliasViewModel.aliasItemState.update {
            AliasItem(mailboxes = listOf(aliasMailbox1, aliasMailbox2))
        }

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem).isEqualTo(
                Initial.aliasItem.copy(
                    mailboxes = listOf(aliasMailbox1, aliasMailbox2)
                )
            )
        }

        val disabledMailboxList = listOf(aliasMailbox1.copy(selected = false), aliasMailbox2)
        baseAliasViewModel.onMailboxesChanged(disabledMailboxList)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem.mailboxes).isEqualTo(disabledMailboxList)
        }
    }

    @Test
    fun `when there are many selected mailboxes mailboxTitle should contain an indicator`() =
        runTest {
            val firstEmail = "test"
            val secondEmail = "test2"

            // Start both as false
            val aliasMailbox1 = SelectedAliasMailboxUiModel(AliasMailboxUiModel(1, firstEmail), false)
            val aliasMailbox2 = SelectedAliasMailboxUiModel(AliasMailboxUiModel(2, secondEmail), false)
            baseAliasViewModel.aliasItemState.update {
                AliasItem(mailboxes = listOf(aliasMailbox1, aliasMailbox2))
            }

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
}
