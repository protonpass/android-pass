/*
 * Copyright (c) 2023-2026 Proton AG
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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolder
import proton.android.pass.data.api.usecases.folders.GetFolder
import proton.android.pass.data.api.usecases.folders.ObserveFolderLimits
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import javax.inject.Inject

@HiltViewModel
class FolderOptionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandleProvider,
    canCreateFolder: CanCreateFolder,
    private val getFolder: GetFolder,
    private val observeFoldersByParentId: ObserveFoldersByParentId,
    observeFolderLimits: ObserveFolderLimits
) : ViewModel() {

    val navShareId: ShareId = savedStateHandle.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    val navFolderId: FolderId = savedStateHandle.get()
        .require<String>(CommonOptionalNavArgId.FolderId.key)
        .let(::FolderId)

    private val folderLimitsFlow = observeFolderLimits()

    val canCreateSubFolder: StateFlow<Boolean> = flow {
        val depth = safeRunCatching {
            computeFolderDepth(navShareId, navFolderId)
        }.getOrElse {
            PassLogger.w(TAG, it, "Failed to compute folder depth")
            emit(false)
            return@flow
        }

        emitAll(
            combine(
                canCreateFolder(navShareId),
                observeFoldersByParentId(navShareId, navFolderId),
                observeFoldersByParentId(navShareId),
                folderLimitsFlow
            ) { canCreate, children, folders, folderLimits ->
                canCreate.isAllowed &&
                    depth < folderLimits.maxDepth &&
                    children.size < folderLimits.maxChildren &&
                    folders.size < folderLimits.maxCount
            }.catch { error ->
                PassLogger.w(TAG, error, "Failed to observe folder limits")
                emit(false)
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false
    )

    private suspend fun computeFolderDepth(shareId: ShareId, folderId: FolderId): Int {
        var depth = 1
        var currentFolderId: FolderId? = folderId
        val visited = mutableSetOf<FolderId>()
        while (currentFolderId != null) {
            if (!visited.add(currentFolderId)) break
            val folder = getFolder(shareId, currentFolderId)
            currentFolderId = folder.parentFolderId
            if (currentFolderId != null) depth++
        }
        return depth
    }

    private companion object {
        private const val TAG = "FolderOptionsViewModel"
    }
}
