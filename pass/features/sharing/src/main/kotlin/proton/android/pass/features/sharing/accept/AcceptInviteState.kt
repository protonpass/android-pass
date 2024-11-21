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

package proton.android.pass.features.sharing.accept

import androidx.compose.runtime.Stable
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon

@Stable
internal sealed interface AcceptInviteState {

    val progress: AcceptInviteProgress

    val event: AcceptInviteEvent

    @Stable
    data object Initial : AcceptInviteState {

        override val progress: AcceptInviteProgress = AcceptInviteProgress.Pending

        override val event: AcceptInviteEvent = AcceptInviteEvent.Idle

    }

    @Stable
    data class ItemInvite(
        override val progress: AcceptInviteProgress,
        override val event: AcceptInviteEvent,
        private val pendingItemInvite: PendingInvite.Item
    ) : AcceptInviteState {

        internal val inviterEmail: String = pendingItemInvite.inviterEmail

    }

    @Stable
    data class VaultInvite(
        override val progress: AcceptInviteProgress,
        override val event: AcceptInviteEvent,
        private val pendingVaultInvite: PendingInvite.Vault
    ) : AcceptInviteState {

        internal val inviterEmail: String = pendingVaultInvite.inviterEmail

        internal val name: String = pendingVaultInvite.name

        internal val itemCount: Int = pendingVaultInvite.itemCount

        internal val memberCount: Int = pendingVaultInvite.memberCount

        internal val icon: ShareIcon = pendingVaultInvite.icon

        internal val color: ShareColor = pendingVaultInvite.color

    }

}
