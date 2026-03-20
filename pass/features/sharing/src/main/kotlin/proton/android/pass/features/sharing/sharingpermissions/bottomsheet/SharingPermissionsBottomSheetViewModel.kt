/*
 * Copyright (c) 2024-2026 Proton AG
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.log.api.PassLogger
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.data.api.repositories.GroupTarget
import proton.android.pass.data.api.repositories.InviteTarget
import proton.android.pass.data.api.repositories.UserTarget
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.ItemId
import proton.android.pass.features.sharing.extensions.toShareRole
import proton.android.pass.features.sharing.sharingpermissions.SharingType
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import javax.inject.Inject

@HiltViewModel
class SharingPermissionsBottomSheetViewModel @Inject constructor(
    private val bulkInviteRepository: BulkInviteRepository,
    private val savedStateHandleProvider: SavedStateHandleProvider,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : ViewModel() {

    private val itemIdOption: Option<ItemId> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.ItemId.key)
        .toOption()
        .map(::ItemId)

    private val mode = getMode()

    private val eventFlow: MutableStateFlow<SharingPermissionsBottomSheetEvent> =
        MutableStateFlow(SharingPermissionsBottomSheetEvent.Unknown)

    internal val state: StateFlow<SharingPermissionsBottomSheetUiState> = combine(
        eventFlow,
        bulkInviteRepository.observeInvites(),
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.RENAME_ADMIN_TO_MANAGER)
    ) { event, invites, isRenameAdminToManagerEnabled ->
        SharingPermissionsBottomSheetUiState(
            event = event,
            mode = mode.toUi(invites),
            displayRemove = when (mode) {
                is SharingPermissionMode.SetAll -> false
                is SharingPermissionMode.SetOne -> true
            },
            itemIdOption = itemIdOption,
            isRenameAdminToManagerEnabled = isRenameAdminToManagerEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SharingPermissionsBottomSheetUiState.initial(
            mode = mode.toUi(emptyList()),
            itemIdOption = itemIdOption
        )
    )

    internal fun onPermissionSelected(sharingType: SharingType) {
        viewModelScope.launch {
            when (mode) {
                is SharingPermissionMode.SetAll -> {
                    bulkInviteRepository.setAllPermissions(sharingType.toShareRole())
                }

                is SharingPermissionMode.SetOne -> {
                    bulkInviteRepository.setPermission(
                        inviteTarget = mode.toInviteTarget(),
                        permission = sharingType.toShareRole()
                    )
                }
            }

            eventFlow.update { SharingPermissionsBottomSheetEvent.Close }
        }
    }

    internal fun onDeleteUser() {
        viewModelScope.launch {
            if (mode is SharingPermissionMode.SetAll) {
                PassLogger.w(TAG, "onDeleteUser called in SetAll mode — this is a programming error")
                return@launch
            }

            bulkInviteRepository.removeInvite((mode as SharingPermissionMode.SetOne).toInviteTarget())
            eventFlow.update { SharingPermissionsBottomSheetEvent.Close }
        }
    }

    internal fun clearEvent() {
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
                val groupId = savedState.get<String>(CommonOptionalNavArgId.GroupId.key)?.let(::GroupId)
                val permissionAsEnum = SharingType.entries.firstOrNull { it.name == permission }
                when (permissionAsEnum) {
                    null -> throw IllegalArgumentException("Unknown permission: $permission")
                    else -> SharingPermissionMode.SetOne(email, permissionAsEnum, groupId)
                }
            }

            EditPermissionsMode.AllUsers -> SharingPermissionMode.SetAll
        }
    }

    private fun SharingPermissionMode.toUi(invites: List<InviteTarget>) = when (this) {
        SharingPermissionMode.SetAll -> SharingPermissionsEditMode.All
        is SharingPermissionMode.SetOne -> {
            val inviteTarget = findMatchingTarget(invites)
            SharingPermissionsEditMode.EditOne(
                displayName = when (inviteTarget) {
                    is GroupTarget -> inviteTarget.name
                    else -> email
                },
                sharingType = currentPermission,
                isGroup = inviteTarget is GroupTarget || groupId != null
            )
        }
    }

    private sealed interface SharingPermissionMode {

        data object SetAll : SharingPermissionMode

        data class SetOne(
            val email: String,
            val currentPermission: SharingType,
            val groupId: GroupId?
        ) : SharingPermissionMode

    }

    private suspend fun SharingPermissionMode.SetOne.toInviteTarget(): InviteTarget {
        val invites = bulkInviteRepository.observeInvites().first()
        return findMatchingTarget(invites) ?: when (groupId) {
            null -> UserTarget(email, currentPermission.toShareRole())
            else -> GroupTarget(
                groupId = groupId,
                name = email,
                memberCount = 0,
                email = email,
                shareRole = currentPermission.toShareRole()
            )
        }
    }

    private fun SharingPermissionMode.SetOne.findMatchingTarget(invites: List<InviteTarget>): InviteTarget? =
        when (groupId) {
            null -> invites.firstOrNull { invite ->
                invite is UserTarget && invite.email == email
            }

            else -> invites.firstOrNull { invite ->
                invite is GroupTarget && invite.groupId == groupId
            }
        }

    companion object {
        private const val TAG = "SharingPermissionsBottomSheetViewModel"
    }
}
