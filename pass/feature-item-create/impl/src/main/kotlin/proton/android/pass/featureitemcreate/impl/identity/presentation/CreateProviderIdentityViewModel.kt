/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.featureitemcreate.impl.identity.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.ShareId
import proton.android.pass.featureitemcreate.impl.common.OptionShareIdSaver
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.common.getShareUiStateFlow
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import javax.inject.Inject

@HiltViewModel
class CreateProviderIdentityViewModel @Inject constructor(
    observeVaults: ObserveVaultsWithItemCount,
    observeDefaultVault: ObserveDefaultVault,
    savedStateHandleProvider: SavedStateHandleProvider,
    identityActionsProvider: IdentityActionsProvider
) : ViewModel(), IdentityActionsProvider by identityActionsProvider {

    private val navShareId: Option<ShareId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map(::ShareId)

    @OptIn(SavedStateHandleSaveableApi::class)
    private var selectedShareIdMutableState: Option<ShareId> by savedStateHandleProvider.get()
        .saveable(stateSaver = OptionShareIdSaver) { mutableStateOf(None) }
    private val selectedShareIdState: Flow<Option<ShareId>> =
        snapshotFlow { selectedShareIdMutableState }
            .filterNotNull()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = None
            )

    private val shareUiState: StateFlow<ShareUiState> = getShareUiStateFlow(
        navShareIdState = flowOf(navShareId),
        selectedShareIdState = selectedShareIdState,
        observeAllVaultsFlow = observeVaults().asLoadingResult(),
        observeDefaultVaultFlow = observeDefaultVault().asLoadingResult(),
        viewModelScope = viewModelScope,
        tag = TAG
    )

    val state: StateFlow<IdentityUiState> = shareUiState.map(IdentityUiState::Success)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = IdentityUiState.NotInitialised
        )

    fun onVaultSelect(shareId: ShareId) {
        selectedShareIdMutableState = Some(shareId)
    }

    fun onSubmit(shareId: ShareId) {
        // Implement
    }

    companion object {
        private const val TAG = "CreateIdentityViewModel"
    }
}

