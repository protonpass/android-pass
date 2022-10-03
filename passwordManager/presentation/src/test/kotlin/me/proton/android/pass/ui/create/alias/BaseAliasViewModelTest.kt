package me.proton.android.pass.ui.create.alias

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.android.pass.ui.MainDispatcherRule
import me.proton.core.pass.presentation.create.alias.BaseAliasViewModel
import me.proton.core.pass.presentation.create.alias.CreateUpdateAliasUiState.Companion.Initial
import me.proton.core.pass.test.core.TestAccountManager
import me.proton.core.pass.test.domain.TestAliasMailbox
import me.proton.core.pass.test.domain.TestAliasSuffix
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class BaseAliasViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var baseAliasViewModel: BaseAliasViewModel

    @Before
    fun setUp() {
        baseAliasViewModel = object : BaseAliasViewModel(TestAccountManager()) {}
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
    fun `when the suffix has changed the state should hold it`() = runTest {
        val aliasMailbox = TestAliasMailbox.create()
        baseAliasViewModel.onMailboxChange(aliasMailbox)

        baseAliasViewModel.aliasUiState.test {
            assertThat(awaitItem().aliasItem)
                .isEqualTo(Initial.aliasItem.copy(selectedMailbox = aliasMailbox))
        }
    }
}
