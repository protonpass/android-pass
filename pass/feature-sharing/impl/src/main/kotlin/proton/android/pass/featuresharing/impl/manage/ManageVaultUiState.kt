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

package proton.android.pass.featuresharing.impl.manage

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import proton.android.pass.data.api.usecases.VaultMember
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount

@Stable
sealed interface ManageVaultEvent {
    @Stable
    object Unknown : ManageVaultEvent

    @Stable
    object Close : ManageVaultEvent

    @Stable
    @JvmInline
    value class ShowInvitesInfo(val shareId: ShareId) : ManageVaultEvent
}

@Stable
sealed interface ShareOptions {
    @Stable
    object Hide : ShareOptions

    @Stable
    data class Show(
        val enableButton: Boolean,
        val subtitle: ShareOptionsSubtitle
    ) : ShareOptions

    sealed interface ShareOptionsSubtitle {

        @JvmInline
        value class RemainingInvites(val remainingInvites: Int) : ShareOptionsSubtitle

        object LimitReached : ShareOptionsSubtitle
    }
}

@Stable
data class ManageVaultUiState(
    val vault: VaultWithItemCount?,
    val content: ManageVaultUiContent,
    val shareOptions: ShareOptions,
    val event: ManageVaultEvent
) {
    companion object {
        val Initial = ManageVaultUiState(
            vault = null,
            content = ManageVaultUiContent.Loading,
            shareOptions = ShareOptions.Hide,
            event = ManageVaultEvent.Unknown
        )
    }
}

@Stable
sealed interface ManageVaultUiContent {
    @Stable
    object Loading : ManageVaultUiContent

    @Stable
    data class Content(
        val vaultMembers: ImmutableList<VaultMember.Member>,
        val invites: ImmutableList<VaultMember>,
        val loadingInvites: ImmutableSet<NewUserInviteId>,
        val canEdit: Boolean,
    ) : ManageVaultUiContent
}
