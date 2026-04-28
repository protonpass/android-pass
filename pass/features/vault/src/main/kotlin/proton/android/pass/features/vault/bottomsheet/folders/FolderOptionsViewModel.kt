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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.folders.GetFolder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import javax.inject.Inject

@HiltViewModel
class FolderOptionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandleProvider,
    private val getFolder: GetFolder
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
            runCatching { computeFolderDepth(navShareId, navFolderId) }
                .onSuccess { depth -> _canCreateSubFolder.update { depth < MAX_FOLDER_DEPTH } }
                .onFailure { PassLogger.w(TAG, it, "Failed to compute folder depth") }
        }
    }

    private suspend fun computeFolderDepth(shareId: ShareId, folderId: FolderId): Int {
        var depth = 1
        var currentFolderId: FolderId? = folderId
        while (currentFolderId != null) {
            val folder = getFolder(shareId, currentFolderId)
            currentFolderId = folder.parentFolderId
            if (currentFolderId != null) depth++
        }
        return depth
    }

    private companion object {
        private const val MAX_FOLDER_DEPTH = 5
        private const val TAG = "FolderOptionsViewModel"
    }
}
