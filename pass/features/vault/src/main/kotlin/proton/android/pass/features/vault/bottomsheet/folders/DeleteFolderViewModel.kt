/*
 * Copyright (c) 2025 Proton AG
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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.folders.DeleteFolders
import proton.android.pass.data.api.usecases.folders.GetFolder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class DeleteFolderViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFolder: GetFolder,
    private val deleteFolders: DeleteFolders,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandle
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val folderId: FolderId = savedStateHandle
        .require<String>(CommonOptionalNavArgId.FolderId.key)
        .let(::FolderId)

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val folderNameFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val eventFlow: MutableStateFlow<DeleteFolderEvent> =
        MutableStateFlow(DeleteFolderEvent.Unknown)
    private val formFlow: MutableStateFlow<FormState> = MutableStateFlow(FormState.Initial)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    init {
        viewModelScope.launch(coroutineExceptionHandler) {
            isLoadingState.update { IsLoadingState.Loading }
            safeRunCatching {
                getFolder(shareId = shareId, folderId = folderId)
            }.onSuccess { folder ->
                folderNameFlow.update { folder.name }
                isLoadingState.update { IsLoadingState.NotLoading }
            }.onFailure { error ->
                PassLogger.w(TAG, "Error getting folder by id")
                PassLogger.w(TAG, error)
                snackbarDispatcher(VaultSnackbarMessage.CannotRetrieveVaultError)
                isLoadingState.update { IsLoadingState.NotLoading }
            }
        }
    }

    internal val state: StateFlow<DeleteFolderUiState> = combine(
        folderNameFlow,
        eventFlow,
        formFlow,
        isLoadingState
    ) { folderName, event, form, isLoadingState ->
        DeleteFolderUiState(
            event = event,
            isLoadingState = isLoadingState,
            folderName = folderName,
            folderText = form.text,
            isButtonEnabled = form.isButtonEnabled
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DeleteFolderUiState.Initial
    )

    internal fun onTextChange(text: String) {
        formFlow.update {
            FormState(
                text = text,
                isButtonEnabled = IsButtonEnabled.from(text == folderNameFlow.value)
            )
        }
    }

    internal fun onDelete() {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }

            runCatching { deleteFolders.invoke(shareId, listOf(folderId)) }
                .onSuccess {
                    snackbarDispatcher(VaultSnackbarMessage.DeleteFolderSuccess)
                    eventFlow.update { DeleteFolderEvent.Deleted }
                }
                .onFailure {
                    PassLogger.w(TAG, "Error deleting folder")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(VaultSnackbarMessage.DeleteFolderError)
                }

            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    private data class FormState(
        val text: String,
        val isButtonEnabled: IsButtonEnabled
    ) {
        companion object {
            val Initial = FormState(
                text = "",
                isButtonEnabled = IsButtonEnabled.Disabled
            )
        }
    }

    private companion object {

        private const val TAG = "DeleteFolderViewModel"

    }

}
