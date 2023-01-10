package proton.android.pass.featurecreateitem.impl.note

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import proton.android.pass.featurecreateitem.impl.note.CreateUpdateNoteUiState.Companion.Initial
import proton.android.pass.notifications.fakes.TestSnackbarMessageRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle
import org.junit.Before
import org.junit.Rule
import org.junit.Test

internal class BaseNoteViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var baseNoteViewModel: BaseNoteViewModel

    @Before
    fun setUp() {
        baseNoteViewModel = object : BaseNoteViewModel(
            TestSnackbarMessageRepository(),
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
        baseNoteViewModel.onTitleChange(titleInput)
        baseNoteViewModel.noteUiState.test {
            assertThat(awaitItem().noteItem)
                .isEqualTo(Initial.noteItem.copy(title = titleInput))
        }
    }

    @Test
    fun `when the note has changed, the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        baseNoteViewModel.onNoteChange(noteInput)
        baseNoteViewModel.noteUiState.test {
            assertThat(awaitItem().noteItem)
                .isEqualTo(Initial.noteItem.copy(note = noteInput))
        }
    }
}
