/*
 * Copyright (c) 2023 Proton AG
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
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.folders.DeleteFolders
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class FolderOptionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandleProvider,
    private val deleteFolders: DeleteFolders,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    val navShareId: ShareId = savedStateHandle.get()
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    val navFolderId: FolderId = savedStateHandle.get()
        .require<String>(CommonOptionalNavArgId.FolderId.key)
        .let(::FolderId)

    private val _state = MutableStateFlow(FolderOptionsUiState.Initial)
    val state: StateFlow<FolderOptionsUiState> = _state.asStateFlow()

    fun onDeleteFolder() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingState = IsLoadingState.Loading) }
            safeRunCatching {
                deleteFolders(navShareId, listOf(navFolderId))
            }.onSuccess {
                snackbarDispatcher(VaultSnackbarMessage.DeleteFolderSuccess)
                _state.update {
                    it.copy(
                        isLoadingState = IsLoadingState.NotLoading,
                        event = FolderOptionsEvent.FolderDeleted
                    )
                }
            }.onFailure { error ->
                PassLogger.w(TAG, "Error deleting folder")
                PassLogger.w(TAG, error)
                snackbarDispatcher(VaultSnackbarMessage.DeleteFolderError)
                _state.update { it.copy(isLoadingState = IsLoadingState.NotLoading) }
            }
        }
    }

    private companion object {
        private const val TAG = "FolderOptionsViewModel"
    }
}

sealed interface FolderOptionsEvent {
    data object Unknown : FolderOptionsEvent
    data object FolderDeleted : FolderOptionsEvent
}

data class FolderOptionsUiState(
    val isLoadingState: IsLoadingState,
    val event: FolderOptionsEvent
) {
    companion object {
        val Initial = FolderOptionsUiState(
            isLoadingState = IsLoadingState.NotLoading,
            event = FolderOptionsEvent.Unknown
        )
    }
}
