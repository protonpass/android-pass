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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.folders.GetFolder
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.FolderLimits
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import javax.inject.Inject

@HiltViewModel
class FolderOptionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandleProvider,
    private val getFolder: GetFolder,
    private val observeFoldersByParentId: ObserveFoldersByParentId
) : ViewModel() {

    val navShareId: ShareId = savedStateHandle.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    val navFolderId: FolderId = savedStateHandle.get()
        .require<String>(CommonOptionalNavArgId.FolderId.key)
        .let(::FolderId)

    private val _canCreateSubFolder = MutableStateFlow(true)
    val canCreateSubFolder: StateFlow<Boolean> = _canCreateSubFolder.asStateFlow()

    init {
        viewModelScope.launch {
            val depth = safeRunCatching {
                computeFolderDepth(navShareId, navFolderId)
            }.onFailure {
                PassLogger.w(TAG, it, "Failed to compute folder depth")
                _canCreateSubFolder.update { false }
            }.getOrNull() ?: return@launch

            combine(
                observeFoldersByParentId(navShareId, navFolderId),
                observeFoldersByParentId(navShareId)
            ) { children, folders ->
                val childrenCount = children.size
                val totalCount = folders.size
                depth < FolderLimits.MAX_FOLDER_DEPTH &&
                    childrenCount < FolderLimits.MAX_FOLDER_WIDTH &&
                    totalCount < FolderLimits.MAX_FOLDERS_PER_VAULT
            }.catch { error ->
                PassLogger.w(TAG, error, "Failed to observe folder limits")
                _canCreateSubFolder.update { false }
            }.collect { canCreateSubFolder ->
                _canCreateSubFolder.update {
                    canCreateSubFolder
                }
            }
        }
    }

    private suspend fun computeFolderDepth(shareId: ShareId, folderId: FolderId): Int {
        var depth = 1
        var currentFolderId: FolderId? = folderId
        val visited = mutableSetOf<FolderId>()
        while (currentFolderId != null && depth <= FolderLimits.MAX_FOLDER_DEPTH) {
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
