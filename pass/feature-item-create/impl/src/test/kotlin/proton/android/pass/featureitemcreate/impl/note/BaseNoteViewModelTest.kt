package proton.android.pass.featureitemcreate.impl.note

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.featureitemcreate.impl.note.BaseNoteUiState.Companion.Initial
import proton.android.pass.notifications.fakes.TestSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.TestSavedStateHandle

internal class BaseNoteViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var snackbarDispatcher: TestSnackbarDispatcher
    private lateinit var baseNoteViewModel: BaseNoteViewModel

    @Before
    fun setUp() {
        snackbarDispatcher = TestSnackbarDispatcher()
        baseNoteViewModel = object : BaseNoteViewModel(
            snackbarDispatcher,
            TestSavedStateHandle.create()
        ) {}
    }

    @Test
    fun `should start with the initial state`() = runTest {
        baseNoteViewModel.baseNoteUiState.test {
            assertThat(awaitItem()).isEqualTo(Initial)
        }
    }

    @Test
    fun `when the title has changed, the state should hold it`() = runTest {
        val titleInput = "Title Changed"
        baseNoteViewModel.onTitleChange(titleInput)
        baseNoteViewModel.baseNoteUiState.test {
            assertThat(awaitItem().noteItem)
                .isEqualTo(Initial.noteItem.copy(title = titleInput))
        }
    }

    @Test
    fun `when the note has changed, the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        baseNoteViewModel.onNoteChange(noteInput)
        baseNoteViewModel.baseNoteUiState.test {
            assertThat(awaitItem().noteItem)
                .isEqualTo(Initial.noteItem.copy(note = noteInput))
        }
    }
}
