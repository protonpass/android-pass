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
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolderResult
import proton.android.pass.data.fakes.usecases.FakeCanCreateFolder
import proton.android.pass.data.fakes.usecases.folders.FakeGetFolder
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFolderLimits
import proton.android.pass.data.fakes.usecases.folders.FakeObserveFoldersByParentId
import proton.android.pass.domain.Folder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.FolderTestFactory

class FolderOptionsViewModelTest {

    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun `can create subfolder updates when child count drops below limit`() = runTest {
        val observeFoldersByParentId = FakeObserveFoldersByParentId()
        observeFoldersByParentId.sendResult(
            SHARE_ID,
            PARENT_FOLDER_ID,
            Result.success(children(count = 10))
        )
        observeFoldersByParentId.sendResult(
            SHARE_ID,
            Result.success(children(count = 10))
        )

        val instance = createInstance(observeFoldersByParentId = observeFoldersByParentId)

        instance.canCreateSubFolder.test {
            val initialCanCreate = awaitItem()
            if (initialCanCreate) {
                assertThat(awaitItem()).isFalse()
            } else {
                assertThat(initialCanCreate).isFalse()
            }

            observeFoldersByParentId.sendResult(
                SHARE_ID,
                PARENT_FOLDER_ID,
                Result.success(children(count = 9))
            )

            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `can create subfolder when current folder is at depth 1 (root)`() = runTest {
        val observeFoldersByParentId = emptyObserve()
        val getFolder = FakeGetFolder().apply {
            setResult(
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = PARENT_FOLDER_ID,
                        parentFolderId = null
                    )
                )
            )
        }

        createInstance(observeFoldersByParentId, getFolder).canCreateSubFolder.test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `can create subfolder when current folder is at depth 4`() = runTest {
        val d1 = FolderId("d1")
        val d2 = FolderId("d2")
        val d3 = FolderId("d3")
        val getFolder = FakeGetFolder().apply {
            setResult(
                PARENT_FOLDER_ID,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = PARENT_FOLDER_ID,
                        parentFolderId = d3
                    )
                )
            )
            setResult(
                d3,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = d3,
                        parentFolderId = d2
                    )
                )
            )
            setResult(
                d2,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = d2,
                        parentFolderId = d1
                    )
                )
            )
            setResult(
                d1,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = d1,
                        parentFolderId = null
                    )
                )
            )
        }

        createInstance(emptyObserve(), getFolder).canCreateSubFolder.test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `cannot create subfolder when current folder is at depth 5 (at limit)`() = runTest {
        val d1 = FolderId("d1")
        val d2 = FolderId("d2")
        val d3 = FolderId("d3")
        val d4 = FolderId("d4")
        val getFolder = FakeGetFolder().apply {
            setResult(
                PARENT_FOLDER_ID,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = PARENT_FOLDER_ID,
                        parentFolderId = d4
                    )
                )
            )
            setResult(
                d4,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = d4,
                        parentFolderId = d3
                    )
                )
            )
            setResult(
                d3,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = d3,
                        parentFolderId = d2
                    )
                )
            )
            setResult(
                d2,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = d2,
                        parentFolderId = d1
                    )
                )
            )
            setResult(
                d1,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = d1,
                        parentFolderId = null
                    )
                )
            )
        }

        createInstance(emptyObserve(), getFolder).canCreateSubFolder.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `cannot create subfolder when getFolder throws (fail-closed)`() = runTest {
        val getFolder = FakeGetFolder().apply {
            setResult(Result.failure(IllegalStateException("Folder not found")))
        }

        createInstance(emptyObserve(), getFolder).canCreateSubFolder.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `handles cycle in parent chain without hanging`() = runTest {
        val cycleId = FolderId("cycle-target")
        val getFolder = FakeGetFolder().apply {
            setResult(
                PARENT_FOLDER_ID,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = PARENT_FOLDER_ID,
                        parentFolderId = cycleId
                    )
                )
            )
            setResult(
                cycleId,
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = cycleId,
                        parentFolderId = PARENT_FOLDER_ID
                    )
                )
            )
        }

        createInstance(emptyObserve(), getFolder).canCreateSubFolder.test {
            awaitItem()
        }
    }

    @Test
    fun `cannot create subfolder when canCreateFolder returns false`() = runTest {
        val canCreateFolder = FakeCanCreateFolder().apply { sendValue(false) }

        createInstance(emptyObserve(), canCreateFolder = canCreateFolder).canCreateSubFolder.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `can create subfolder updates when canCreateFolder flips to true`() = runTest {
        val canCreateFolder = FakeCanCreateFolder().apply { sendValue(false) }

        createInstance(emptyObserve(), canCreateFolder = canCreateFolder).canCreateSubFolder.test {
            assertThat(awaitItem()).isFalse()

            canCreateFolder.sendValue(true)

            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `canManageFolderStructure is false when planAllows is false (free user)`() = runTest {
        val canCreateFolder = FakeCanCreateFolder().apply {
            sendValue(CanCreateFolderResult(roleAllows = true, planAllows = false))
        }

        createInstance(emptyObserve(), canCreateFolder = canCreateFolder)
            .canManageFolderStructure
            .test {
                assertThat(awaitItem()).isFalse()
            }
    }

    @Test
    fun `canManageFolderStructure is true when planAllows is true (paid user)`() = runTest {
        val canCreateFolder = FakeCanCreateFolder().apply {
            sendValue(CanCreateFolderResult(roleAllows = true, planAllows = true))
        }

        createInstance(emptyObserve(), canCreateFolder = canCreateFolder)
            .canManageFolderStructure
            .test {
                assertThat(awaitItem()).isTrue()
            }
    }

    @Test
    fun `canManageFolderStructure updates when planAllows flips`() = runTest {
        val canCreateFolder = FakeCanCreateFolder().apply {
            sendValue(CanCreateFolderResult(roleAllows = true, planAllows = false))
        }

        createInstance(emptyObserve(), canCreateFolder = canCreateFolder)
            .canManageFolderStructure
            .test {
                assertThat(awaitItem()).isFalse()

                canCreateFolder.sendValue(CanCreateFolderResult(roleAllows = true, planAllows = true))

                assertThat(awaitItem()).isTrue()
            }
    }

    private fun createInstance(
        observeFoldersByParentId: FakeObserveFoldersByParentId,
        getFolder: FakeGetFolder = FakeGetFolder().apply {
            setResult(
                Result.success(
                    FolderTestFactory.create(
                        shareId = SHARE_ID,
                        folderId = PARENT_FOLDER_ID
                    )
                )
            )
        },
        canCreateFolder: FakeCanCreateFolder = FakeCanCreateFolder(),
        observeFolderLimits: FakeObserveFolderLimits = FakeObserveFolderLimits()
    ) = FolderOptionsViewModel(
        savedStateHandle = FakeSavedStateHandleProvider().apply {
            get()[CommonNavArgId.ShareId.key] = SHARE_ID.id
            get()[CommonOptionalNavArgId.FolderId.key] = PARENT_FOLDER_ID.id
        },
        canCreateFolder = canCreateFolder,
        getFolder = getFolder,
        observeFoldersByParentId = observeFoldersByParentId,
        observeFolderLimits = observeFolderLimits
    )

    private fun emptyObserve(): FakeObserveFoldersByParentId = FakeObserveFoldersByParentId().apply {
        sendResult(SHARE_ID, PARENT_FOLDER_ID, Result.success(emptyList()))
        sendResult(SHARE_ID, Result.success(emptyList()))
    }

    private fun children(count: Int): List<Folder> = (0 until count).map { index ->
        FolderTestFactory.create(
            shareId = SHARE_ID,
            folderId = FolderId("child-$index"),
            parentFolderId = PARENT_FOLDER_ID
        )
    }

    private companion object {
        private val SHARE_ID = ShareId("FolderOptionsViewModelTest-ShareId")
        private val PARENT_FOLDER_ID = FolderId("FolderOptionsViewModelTest-ParentFolderId")
    }
}
