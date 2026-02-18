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

package proton.android.pass.features.vault.folders

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
import proton.android.pass.data.api.usecases.folders.CreateFolder
import proton.android.pass.data.api.usecases.folders.GetFolder
import proton.android.pass.data.api.usecases.folders.ObserveFoldersByParentId
import proton.android.pass.data.api.usecases.folders.UpdateFolder
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class AddFolderToVaultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getFolder: GetFolder,
    private val createFolder: CreateFolder,
    private val updateFolder: UpdateFolder,
    private val observeFoldersByParentId: ObserveFoldersByParentId,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandle
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val parentFolderId: FolderId? = savedStateHandle
        .get<String?>(ParentFolderIdNavArgId.key)
        ?.let(::FolderId)

    private val editFolderId: FolderId? = savedStateHandle
        .get<String?>(CommonOptionalNavArgId.FolderId.key)
        ?.let(::FolderId)

    private val isEditMode: Boolean = editFolderId != null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val eventFlow: MutableStateFlow<AddFolderToVaultEvent> =
        MutableStateFlow(AddFolderToVaultEvent.Unknown)
    private val formFlow: MutableStateFlow<FormState> = MutableStateFlow(FormState.Initial)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val siblingFoldersFlow = observeFoldersByParentId(shareId, parentFolderId)

    internal val state: StateFlow<AddFolderToVaultUiState> = combine(
        eventFlow,
        formFlow,
        isLoadingState,
        siblingFoldersFlow
    ) { event, form, isLoadingState, siblings ->
        val hasDuplicateName = siblings.any { sibling ->
            sibling.name.equals(form.text, ignoreCase = true) &&
                sibling.folderId != editFolderId
        }

        AddFolderToVaultUiState(
            folderName = form.text,
            isButtonEnabled = if (hasDuplicateName) {
                IsButtonEnabled.Disabled
            } else {
                form.isButtonEnabled
            },
            isLoadingState = isLoadingState,
            event = event,
            showSameFolderExist = hasDuplicateName && form.text.isNotEmpty(),
            isEditMode = isEditMode
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AddFolderToVaultUiState.Initial
    )

    internal fun onStart() {
        if (isEditMode && editFolderId != null) {
            viewModelScope.launch(coroutineExceptionHandler) {
                isLoadingState.update { IsLoadingState.Loading }
                safeRunCatching {
                    getFolder(shareId, editFolderId)
                }.onSuccess { folder ->
                    formFlow.update {
                        FormState(
                            text = folder.name,
                            isButtonEnabled = IsButtonEnabled.Enabled
                        )
                    }
                    isLoadingState.update { IsLoadingState.NotLoading }
                }.onFailure { error ->
                    PassLogger.w(TAG, "Error getting folder")
                    PassLogger.w(TAG, error)
                    snackbarDispatcher(VaultSnackbarMessage.CannotRetrieveVaultError)
                    isLoadingState.update { IsLoadingState.NotLoading }
                }
            }
        }
    }

    internal fun onTextChange(text: String) {
        formFlow.update {
            FormState(
                text = text,
                isButtonEnabled = IsButtonEnabled.from(text.isNotEmpty())
            )
        }
    }

    internal fun onAddFolder() {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            val folderName = formFlow.value.text

            if (isEditMode && editFolderId != null) {
                safeRunCatching {
                    updateFolder(shareId, editFolderId, folderName)
                }.onSuccess {
                    snackbarDispatcher(VaultSnackbarMessage.UpdateFolderSuccess)
                    eventFlow.update { AddFolderToVaultEvent.FolderAdded }
                }.onFailure { error ->
                    PassLogger.w(TAG, "Error updating folder")
                    PassLogger.w(TAG, error)
                    snackbarDispatcher(VaultSnackbarMessage.UpdateFolderError)
                }
            } else {
                safeRunCatching {
                    createFolder(shareId, parentFolderId, folderName)
                }.onSuccess {
                    snackbarDispatcher(VaultSnackbarMessage.CreateFolderSuccess)
                    eventFlow.update { AddFolderToVaultEvent.FolderAdded }
                }.onFailure { error ->
                    PassLogger.w(TAG, "Error creating folder")
                    PassLogger.w(TAG, error)
                    snackbarDispatcher(VaultSnackbarMessage.CreateFolderError)
                }
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
        private const val TAG = "AddFolderToVaultViewModel"
    }
}
