package proton.android.pass.featureitemcreate.impl.note

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.fakes.usecases.TestObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.note.CreateUpdateNoteUiState.Companion.Initial
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import proton.pass.domain.VaultWithItemCount

internal class BaseNoteViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var observeVaults: TestObserveVaultsWithItemCount
    private lateinit var baseNoteViewModel: BaseNoteViewModel

    @Before
    fun setUp() {
        observeVaults = TestObserveVaultsWithItemCount()
        baseNoteViewModel = object : BaseNoteViewModel(
            observeVaults,
            TestSavedStateHandle.create()
        ) {}
    }

    @Test
    fun `should start with the initial state`() = runTest {
        baseNoteViewModel.noteUiState.test {
            assertThat(awaitItem()).isEqualTo(Initial)
        }
    }

    @Test
    fun `when the title has changed, the state should hold it`() = runTest {
        val titleInput = "Title Changed"
        givenAVaultList()
        baseNoteViewModel.onTitleChange(titleInput)
        baseNoteViewModel.noteUiState.test {
            assertThat(awaitItem().noteItem)
                .isEqualTo(Initial.noteItem.copy(title = titleInput))
        }
    }

    @Test
    fun `when the note has changed, the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        givenAVaultList()
        baseNoteViewModel.onNoteChange(noteInput)
        baseNoteViewModel.noteUiState.test {
            assertThat(awaitItem().noteItem)
                .isEqualTo(Initial.noteItem.copy(note = noteInput))
        }
    }

    private fun givenAVaultList() {
        observeVaults.sendResult(
            LoadingResult.Success(
                listOf(
                    VaultWithItemCount(
                        vault = Vault(ShareId("ShareId"), "name"),
                        activeItemCount = 1,
                        trashedItemCount = 0
                    )
                )
            )
        )
    }
}
