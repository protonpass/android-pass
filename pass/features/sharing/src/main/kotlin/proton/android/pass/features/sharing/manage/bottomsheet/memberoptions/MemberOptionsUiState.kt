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

package proton.android.pass.features.sharing.manage.bottomsheet.memberoptions

import androidx.compose.runtime.Stable
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole

enum class MemberPermissionLevel {
    Admin,
    Write,
    Read;

    fun toLoadingOption(): LoadingOption = when (this) {
        Admin -> LoadingOption.Admin
        Write -> LoadingOption.Write
        Read -> LoadingOption.Read
    }

    fun toShareRole(): ShareRole = when (this) {
        Admin -> ShareRole.Admin
        Write -> ShareRole.Write
        Read -> ShareRole.Read
    }
}

enum class LoadingOption {
    Admin,
    Write,
    Read,
    RemoveMember
}

sealed interface MemberOptionsUiEvent {
    @JvmInline
    value class SetPermission(val permission: MemberPermissionLevel) : MemberOptionsUiEvent
    data object TransferOwnership : MemberOptionsUiEvent
    data object RemoveMember : MemberOptionsUiEvent
}

enum class TransferOwnershipState {
    Hide,
    Disabled,
    Enabled
}


@Stable
sealed interface MemberOptionsEvent {
    @Stable
    data object Unknown : MemberOptionsEvent

    @Stable
    @JvmInline
    value class Close(val refresh: Boolean) : MemberOptionsEvent

    @Stable
    data class TransferOwnership(
        val shareId: ShareId,
        val destShareId: ShareId,
        val destEmail: String
    ) : MemberOptionsEvent
}

@Stable
data class MemberOptionsUiState(
    val memberRole: ShareRole,
    val transferOwnership: TransferOwnershipState,
    val event: MemberOptionsEvent,
    val loadingOption: LoadingOption?,
    val isLoading: IsLoadingState,
    val isRenameAdminToManagerEnabled: Boolean
) {
    companion object {
        val Initial = MemberOptionsUiState(
            memberRole = ShareRole.Read,
            transferOwnership = TransferOwnershipState.Hide,
            event = MemberOptionsEvent.Unknown,
            loadingOption = null,
            isLoading = IsLoadingState.NotLoading,
            isRenameAdminToManagerEnabled = false
        )
    }
}
