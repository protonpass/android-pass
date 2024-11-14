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

package proton.android.pass.features.sharing.sharingpermissions.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.features.sharing.extensions.toShareRole
import proton.android.pass.features.sharing.sharingpermissions.SharingType
import javax.inject.Inject

@HiltViewModel
class SharingPermissionsBottomSheetViewModel @Inject constructor(
    private val bulkInviteRepository: BulkInviteRepository,
    private val savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val mode = getMode()

    private val eventFlow: MutableStateFlow<SharingPermissionsBottomSheetEvent> =
        MutableStateFlow(SharingPermissionsBottomSheetEvent.Unknown)

    internal val state: StateFlow<SharingPermissionsBottomSheetUiState> = combine(
        eventFlow,
        bulkInviteRepository.observeAddresses()
    ) { event, addresses ->
        SharingPermissionsBottomSheetUiState(
            event = event,
            mode = mode.toUi(),
            displayRemove = when (mode) {
                is SharingPermissionMode.SetAll -> false
                is SharingPermissionMode.SetOne -> { addresses.size > 1 }
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SharingPermissionsBottomSheetUiState.initial(mode = mode.toUi())
    )

    fun onPermissionSelected(sharingType: SharingType) = viewModelScope.launch {
        when (mode) {
            is SharingPermissionMode.SetAll -> {
                bulkInviteRepository.setAllPermissions(sharingType.toShareRole())
            }
            is SharingPermissionMode.SetOne -> {
                bulkInviteRepository.setPermission(
                    address = mode.email,
                    permission = sharingType.toShareRole()
                )
            }
        }
        eventFlow.update { SharingPermissionsBottomSheetEvent.Close }
    }

    fun onDeleteUser() = viewModelScope.launch {
        when (mode) {
            is SharingPermissionMode.SetAll -> {
                throw IllegalStateException("Cannot delete user in SetAll mode")
            }
            is SharingPermissionMode.SetOne -> {
                bulkInviteRepository.removeAddress(mode.email)
            }
        }
        eventFlow.update { SharingPermissionsBottomSheetEvent.Close }
    }

    fun clearEvent() = viewModelScope.launch {
        eventFlow.update { SharingPermissionsBottomSheetEvent.Unknown }
    }

    private fun getMode(): SharingPermissionMode {
        val savedState = savedStateHandleProvider.get()
        val mode = savedState.require<String>(EditPermissionsModeNavArgId.key)
        val modeAsEnum = EditPermissionsMode.entries.firstOrNull { it.name == mode }
        return when (modeAsEnum) {
            null -> throw IllegalArgumentException("Unknown mode: $mode")
            EditPermissionsMode.SingleUser -> {
                val email = savedState.require<String>(EmailNavArgId.key)
                val permission = savedState.require<String>(PermissionNavArgId.key)
                val permissionAsEnum = SharingType.entries.firstOrNull { it.name == permission }
                when (permissionAsEnum) {
                    null -> throw IllegalArgumentException("Unknown permission: $permission")
                    else -> SharingPermissionMode.SetOne(email, permissionAsEnum)
                }
            }
            EditPermissionsMode.AllUsers -> SharingPermissionMode.SetAll
        }
    }

    private fun SharingPermissionMode.toUi() = when (this) {
        SharingPermissionMode.SetAll -> SharingPermissionsEditMode.All
        is SharingPermissionMode.SetOne -> SharingPermissionsEditMode.EditOne(
            email = email,
            sharingType = currentPermission
        )
    }

    sealed interface SharingPermissionMode {
        data object SetAll : SharingPermissionMode
        data class SetOne(
            val email: String,
            val currentPermission: SharingType
        ) : SharingPermissionMode
    }

}
