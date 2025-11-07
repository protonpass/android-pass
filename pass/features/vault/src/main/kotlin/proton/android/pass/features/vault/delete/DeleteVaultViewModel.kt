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

package proton.android.pass.features.vault.delete

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.DeleteVault
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveEncryptedItems
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.features.vault.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class DeleteVaultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeEncryptedItems: ObserveEncryptedItems,
    private val getVaultByShareId: GetVaultByShareId,
    private val deleteVault: DeleteVault,
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    private val shareId: ShareId = savedStateHandle
        .require<String>(CommonNavArgId.ShareId.key)
        .let(::ShareId)

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val vaultNameFlow: MutableStateFlow<String> = MutableStateFlow("")
    private val eventFlow: MutableStateFlow<DeleteVaultEvent> =
        MutableStateFlow(DeleteVaultEvent.Unknown)
    private val formFlow: MutableStateFlow<FormState> = MutableStateFlow(FormState.Initial)
    private val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    private val sharedItemsCountFlow = observeEncryptedItems(
        selection = ShareSelection.Share(shareId),
        itemState = ItemState.Active,
        filter = ItemTypeFilter.All,
        includeHidden = false
    ).mapLatest { encryptedItems ->
        encryptedItems.filter { it.isShared }.size
    }

    internal val state: StateFlow<DeleteVaultUiState> = combine(
        vaultNameFlow,
        eventFlow,
        formFlow,
        isLoadingState,
        sharedItemsCountFlow
    ) { vaultName, event, form, isLoadingState, sharedItemsCount ->
        DeleteVaultUiState(
            event = event,
            vaultText = form.text,
            isButtonEnabled = form.isButtonEnabled,
            isLoadingState = isLoadingState,
            vaultName = vaultName,
            sharedItemsCount = sharedItemsCount
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = DeleteVaultUiState.Initial
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
                            vaultNameFlow.update { res.data.name }
                            isLoadingState.update { IsLoadingState.NotLoading }
                        }
                    }
                }
        }
    }

    internal fun onTextChange(text: String) {
        formFlow.update {
            FormState(
                text = text,
                isButtonEnabled = IsButtonEnabled.from(text == vaultNameFlow.value)
            )
        }
    }

    internal fun onDelete() {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }

            runCatching { deleteVault.invoke(shareId) }
                .onSuccess {
                    snackbarDispatcher(VaultSnackbarMessage.DeleteVaultSuccess)
                    eventFlow.update { DeleteVaultEvent.Deleted }
                }
                .onFailure {
                    PassLogger.w(TAG, "Error deleting vault")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(VaultSnackbarMessage.DeleteVaultError)
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

        private const val TAG = "DeleteVaultViewModel"

    }

}
