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
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class AddFolderToVaultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getVaultByShareId: GetVaultByShareId,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandle
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    // if not null it means the user want to add a folder inside another one
    private val folderId: FolderId? = savedStateHandle
        .get<String?>(CommonOptionalNavArgId.FolderId.key)
        ?.let { FolderId(it) }

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val eventFlow: MutableStateFlow<AddFolderToVaultEvent> =
        MutableStateFlow(AddFolderToVaultEvent.Unknown)
    private val formFlow: MutableStateFlow<FormState> = MutableStateFlow(FormState.Initial)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val showSameFolderExist = MutableStateFlow(false)
    private val vault: MutableStateFlow<Vault?> = MutableStateFlow(null)


    internal val state: StateFlow<AddFolderToVaultUiState> = combine(
        eventFlow,
        formFlow,
        isLoadingState,
        showSameFolderExist
    ) { event, form, isLoadingState, showSameFolderExist ->
        AddFolderToVaultUiState(
            folderName = form.text,
            isButtonEnabled = form.isButtonEnabled,
            isLoadingState = isLoadingState,
            event = event,
            showSameFolderExist = showSameFolderExist
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = AddFolderToVaultUiState.Initial
    )

    internal fun onStart() {
        viewModelScope.launch(coroutineExceptionHandler) {
            getVaultByShareId(shareId = shareId)
                .asLoadingResult()
                .collect { res ->
                    when (res) {
                        LoadingResult.Loading -> {
                            isLoadingState.update { IsLoadingState.Loading }
                        }

                        is LoadingResult.Error -> {
                            PassLogger.w(TAG, "Error getting vault by id")
                            PassLogger.w(TAG, res.exception)
                            snackbarDispatcher(VaultSnackbarMessage.CannotRetrieveVaultError)
                            isLoadingState.update { IsLoadingState.NotLoading }
                        }

                        is LoadingResult.Success -> {
                            // save vault
                            vault.update { res.data }
                            isLoadingState.update { IsLoadingState.NotLoading }
                        }
                    }
                }
        }
    }

    internal fun onTextChange(text: String) {
        val sameFolderExist = vault.value?.folders?.any { it.name == text } ?: false
        showSameFolderExist.update { sameFolderExist }
        formFlow.update {
            FormState(
                text = text,
                isButtonEnabled = IsButtonEnabled.from(
                    text.isNotEmpty() && !sameFolderExist
                )
            )
        }
    }

    internal fun onAddFolder() {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            // new usecase
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
