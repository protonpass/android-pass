/*
 * Copyright (c) 2026 Proton AG
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


package proton.android.pass.features.vault.bottomsheet.folders

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import proton.android.pass.commonui.fakes.FakeSavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.data.fakes.usecases.folders.FakeDeleteFolders
import proton.android.pass.data.fakes.usecases.folders.FakeGetFolder
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFolderItemCount
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.fakes.FakeSnackbarDispatcher
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.FolderTestFactory

class DeleteFolderViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    private val observeFolderItemCount = FakeObserveFolderItemCount()
    private val getFolder = FakeGetFolder().apply {
        setResult(
            Result.success(
                FolderTestFactory.create(shareId = SHARE_ID, folderId = FOLDER_ID, name = "Work")
            )
        )
    }

    @Test
    fun `item count is null while the count is loading`() = runTest {
        val instance = createInstance()

        instance.state.test {
            assertThat(awaitItem().itemCount).isNull()
        }
    }

    @Test
    fun `item count is the number emitted for the folder`() = runTest {
        observeFolderItemCount.sendResult(Result.success(3))
        val instance = createInstance()

        instance.state.test {
            var state = awaitItem()
            while (state.itemCount == null) state = awaitItem()
            assertThat(state.itemCount).isEqualTo(3)
        }
        assertThat(observeFolderItemCount.lastShareId).isEqualTo(SHARE_ID)
        assertThat(observeFolderItemCount.lastFolderId).isEqualTo(FOLDER_ID)
    }

    @Test
    fun `item count is zero for an empty folder`() = runTest {
        observeFolderItemCount.sendResult(Result.success(0))
        val instance = createInstance()

        instance.state.test {
            var state = awaitItem()
            while (state.itemCount == null) state = awaitItem()
            assertThat(state.itemCount).isEqualTo(0)
        }
    }

    @Test
    fun `item count falls back to null and deletion stays possible when counting fails`() = runTest {
        observeFolderItemCount.sendResult(Result.failure(IllegalStateException("boom")))
        val instance = createInstance()

        instance.onTextChange("Work")

        instance.state.test {
            var state = awaitItem()
            while (state.isButtonEnabled != IsButtonEnabled.Enabled) state = awaitItem()
            assertThat(state.itemCount).isNull()
        }
    }

    private fun createInstance() = DeleteFolderViewModel(
        savedStateHandle = FakeSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = SHARE_ID.id
            get()[CommonOptionalNavArgId.FolderId.key] = FOLDER_ID.id
        }.get(),
        getFolder = getFolder,
        deleteFolders = FakeDeleteFolders(),
        observeFolderItemCount = observeFolderItemCount,
        snackbarDispatcher = FakeSnackbarDispatcher()
    )

    private companion object {
        val SHARE_ID = ShareId("share-id")
        val FOLDER_ID = FolderId("folder-id")
    }
}
