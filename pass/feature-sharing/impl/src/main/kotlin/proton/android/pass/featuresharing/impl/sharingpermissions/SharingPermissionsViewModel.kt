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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.featuresharing.impl.EmailNavArgId
import proton.android.pass.featuresharing.impl.SharingWithUserModeArgId
import proton.android.pass.featuresharing.impl.SharingWithUserModeType
import proton.android.pass.navigation.api.CommonNavArgId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class SharingPermissionsViewModel @Inject constructor(
    getVaultById: GetVaultById,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId =
        ShareId(savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key))
    private val email: String = savedStateHandleProvider.get().require(EmailNavArgId.key)
    private val userMode: SharingWithUserModeType = SharingWithUserModeType
        .values()
        .first {
            it.name == savedStateHandleProvider.get().require(SharingWithUserModeArgId.key)
        }

    private val sharingTypeState: MutableStateFlow<SharingType> = MutableStateFlow(SharingType.Read)
    private val eventState: MutableStateFlow<SharingPermissionsEvents> =
        MutableStateFlow(SharingPermissionsEvents.Unknown)

    val state: StateFlow<SharingPermissionsUIState> = combine(
        flowOf(email),
        getVaultById(shareId = shareId).asLoadingResult(),
        sharingTypeState,
        eventState
    ) { email, vault, sharingType, event ->
        SharingPermissionsUIState(
            email = email,
            vaultName = vault.getOrNull()?.name,
            sharingType = sharingType,
            event = event
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingPermissionsUIState()
    )

    fun onPermissionChange(sharingType: SharingType) {
        sharingTypeState.update { sharingType }
    }

    fun onPermissionsSubmit() {
        eventState.update {
            SharingPermissionsEvents.NavigateToSummary(
                shareId = shareId,
                email = email,
                permission = sharingTypeState.value.ordinal,
                mode = userMode
            )
        }
    }

    fun clearEvent() {
        eventState.update { SharingPermissionsEvents.Unknown }
    }
}
