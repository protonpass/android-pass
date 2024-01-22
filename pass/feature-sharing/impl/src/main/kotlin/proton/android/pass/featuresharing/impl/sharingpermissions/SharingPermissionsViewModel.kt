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

package proton.android.pass.featuresharing.impl.sharingpermissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.domain.ShareId
import proton.android.pass.featuresharing.impl.common.toUiState
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class SharingPermissionsViewModel @Inject constructor(
    bulkInviteRepository: BulkInviteRepository,
    getVaultById: GetVaultById,
    savedStateHandleProvider: SavedStateHandleProvider,
) : ViewModel() {

    private val shareId: ShareId = ShareId(
        id = savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key)
    )

    private val eventState: MutableStateFlow<SharingPermissionsEvents> =
        MutableStateFlow(SharingPermissionsEvents.Unknown)

    val state: StateFlow<SharingPermissionsUIState> = combine(
        bulkInviteRepository.observeAddresses(),
        getVaultById(shareId = shareId).asLoadingResult(),
        eventState
    ) { addresses, vault, event ->
        val uiEvent = if (event == SharingPermissionsEvents.Unknown && addresses.isEmpty()) {
            SharingPermissionsEvents.BackToHome
        } else {
            event
        }
        SharingPermissionsUIState(
            addresses = addresses.map { it.toUiState() }.toImmutableList(),
            headerState = SharingPermissionsHeaderState(
                memberCount = addresses.size
            ),
            vaultName = vault.getOrNull()?.name,
            event = uiEvent
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingPermissionsUIState()
    )

    fun onPermissionsSubmit() = viewModelScope.launch {
        eventState.update {
            SharingPermissionsEvents.NavigateToSummary(shareId = shareId)
        }
    }

    fun clearEvent() {
        eventState.update { SharingPermissionsEvents.Unknown }
    }

}
