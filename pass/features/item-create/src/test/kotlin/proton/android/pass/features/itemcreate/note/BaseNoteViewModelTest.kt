/*
 * Copyright (c) 2023-2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemcreate.note

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import proton.android.pass.clipboard.fakes.FakeClipboardManager
import proton.android.pass.commonpresentation.fakes.attachments.FakeAttachmentHandler
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeCanPerformPaidAction
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepositoryImpl
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandlerImpl
import proton.android.pass.features.itemcreate.common.formprocessor.FakeNoteItemFormProcessor
import proton.android.pass.features.itemcreate.note.BaseNoteUiState.Companion.Initial
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.preferences.FakePreferenceRepository
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.totp.fakes.FakeTotpManager

internal class BaseNoteViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private lateinit var canPerformPaidAction: FakeCanPerformPaidAction
    private lateinit var snackbarDispatcher: FakeSnackbarDispatcher
    private lateinit var savedStateHandleProvider: FakeSavedStateHandleProvider
    private lateinit var baseNoteViewModel: BaseNoteViewModel

    @Before
    fun setUp() {
        snackbarDispatcher = FakeSnackbarDispatcher()
        savedStateHandleProvider = FakeSavedStateHandleProvider()
        canPerformPaidAction = FakeCanPerformPaidAction()
        baseNoteViewModel = object : BaseNoteViewModel(
            snackbarDispatcher = snackbarDispatcher,
            attachmentsHandler = FakeAttachmentHandler(),
            userPreferencesRepository = FakePreferenceRepository(),
            savedStateHandleProvider = savedStateHandleProvider,
            customFieldDraftRepository = CustomFieldDraftRepositoryImpl(),
            customFieldHandler = CustomFieldHandlerImpl(FakeTotpManager(), FakeEncryptionContextProvider()),
            canPerformPaidAction = canPerformPaidAction,
            clipboardManager = FakeClipboardManager(),
            encryptionContextProvider = FakeEncryptionContextProvider(),
            noteItemFormProcessor = FakeNoteItemFormProcessor()
        ) {}
    }

    @Test
    fun `should start with the initial state`() = runTest {
        canPerformPaidAction.setResult(false)
        baseNoteViewModel.baseNoteUiState.test {
            assertThat(awaitItem()).isEqualTo(Initial)
        }
    }

    @Test
    fun `when the title has changed, the state should hold it`() = runTest {
        val titleInput = "Title Changed"
        baseNoteViewModel.onTitleChange(titleInput)

        assertThat(baseNoteViewModel.noteItemFormState.title).isEqualTo(titleInput)
    }

    @Test
    fun `when the note has changed, the state should hold it`() = runTest {
        val noteInput = "Note Changed"
        baseNoteViewModel.onNoteChange(noteInput)
        assertThat(baseNoteViewModel.noteItemFormState.note).isEqualTo(noteInput)
    }
}
