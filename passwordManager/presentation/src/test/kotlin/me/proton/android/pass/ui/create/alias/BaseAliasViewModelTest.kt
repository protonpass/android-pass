package me.proton.android.pass.ui.create.alias

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import me.proton.android.pass.ui.MainDispatcherRule
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.presentation.create.alias.AliasItem
import me.proton.pass.presentation.create.alias.AliasMailboxUiModel
import me.proton.pass.presentation.create.alias.BaseAliasViewModel
import me.proton.pass.presentation.create.alias.CreateUpdateAliasUiState.Companion.Initial
import me.proton.pass.test.core.TestSavedStateHandle
import me.proton.pass.test.domain.TestAliasMailbox
import me.proton.pass.test.domain.TestAliasSuffix
import me.proton.pass.test.notification.TestSnackbarMessageRepository
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
            TestSavedStateHandle.create()
        ) {}
    }

    @Test
    fun `should start with the initial state`() = runTest {
        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem()).isEqualTo(Initial)
        }
    }

    @Test
    fun `when the title has changed the state should hold it`() = runTest {
        val titleInput = "Title Changed"
        baseAliasViewModel.onTitleChange(titleInput)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(Initial.aliasItem.copy(title = titleInput))
        }
    }

    @Test
    fun `given no suffix when the alias has changed the state should hold it`() = runTest {
        val aliasInput = "aliasInput"
        baseAliasViewModel.onAliasChange(aliasInput)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(Initial.aliasItem.copy(alias = aliasInput))
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
        val aliasSuffix = TestAliasSuffix.create()
        baseAliasViewModel.onSuffixChange(aliasSuffix)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(Initial.aliasItem.copy(selectedSuffix = aliasSuffix))
        }
    }

    @Test
    fun `when the mailbox has changed the state should hold it`() = runTest {
        val aliasMailbox = AliasMailboxUiModel(TestAliasMailbox.create(), true)
        baseAliasViewModel.aliasItemState.update {
            AliasItem(mailboxes = listOf(aliasMailbox))
        }
        baseAliasViewModel.onMailboxChange(aliasMailbox)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(
                    Initial.aliasItem.copy(
                        mailboxes = listOf(
                            aliasMailbox.copy(
                                selected = false
                            )
                        )
                    )
                )
        }
    }

    @Test
    fun `when there are no selected mailboxes isMailboxListApplicable should be false`() = runTest {
        // Start as true
        val aliasMailbox = AliasMailboxUiModel(TestAliasMailbox.create(), true)
        baseAliasViewModel.aliasItemState.update {
            AliasItem(mailboxes = listOf(aliasMailbox))
        }

        // With this change, set it as false
        baseAliasViewModel.onMailboxChange(aliasMailbox)

        baseAliasViewModel.aliasUiState.test {
            val item = awaitItem().aliasItem
            assertThat(item.isMailboxListApplicable).isFalse()
            assertThat(item.mailboxes.size).isEqualTo(1)
            assertThat(item.mailboxes[0].selected).isFalse()
        }
    }

    @Test
    fun `when there are selected mailboxes isMailboxListApplicable should be true`() = runTest {
        // Start as false
        val aliasMailbox = AliasMailboxUiModel(TestAliasMailbox.create(), false)
        baseAliasViewModel.aliasItemState.update {
            AliasItem(mailboxes = listOf(aliasMailbox))
        }

        // With this change, set it as false
        baseAliasViewModel.onMailboxChange(aliasMailbox)

        baseAliasViewModel.aliasUiState.test {
            val item = awaitItem().aliasItem
            assertThat(item.isMailboxListApplicable).isTrue()
            assertThat(item.mailboxes.size).isEqualTo(1)
            assertThat(item.mailboxes[0].selected).isTrue()
        }
    }

    @Test
    fun `when there are many selected mailboxes mailboxTitle should contain an indicator`() =
        runTest {
            val firstEmail = "test"

            // Start both as false
            val aliasMailbox1 = AliasMailboxUiModel(AliasMailbox(1, firstEmail), false)
            val aliasMailbox2 = AliasMailboxUiModel(AliasMailbox(2, "test2"), false)
            baseAliasViewModel.aliasItemState.update {
                AliasItem(mailboxes = listOf(aliasMailbox1, aliasMailbox2))
            }

            // Set both to true
            baseAliasViewModel.onMailboxChange(aliasMailbox1)
            baseAliasViewModel.onMailboxChange(aliasMailbox2)

            baseAliasViewModel.aliasUiState.test {
                val item = awaitItem().aliasItem

                assertThat(item.isMailboxListApplicable).isTrue()
                assertThat(item.mailboxes.size).isEqualTo(2)
                assertThat(item.mailboxes).isEqualTo(
                    listOf(
                        aliasMailbox1.copy(selected = true),
                        aliasMailbox2.copy(selected = true)
                    )
                )
                assertThat(item.mailboxTitle).isEqualTo("$firstEmail (1+)")
            }
        }
}
