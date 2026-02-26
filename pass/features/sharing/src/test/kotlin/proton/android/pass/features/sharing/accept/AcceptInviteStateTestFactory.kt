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

package proton.android.pass.features.sharing.accept

import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon

internal object AcceptInviteStateTestFactory {

    internal object Item {

        internal fun create(
            progress: AcceptInviteProgress = AcceptInviteProgress.Pending,
            event: AcceptInviteEvent = AcceptInviteEvent.Idle,
            invite: AcceptInviteUiModel.Item = AcceptInviteUiModel.Item.User(
                inviterEmail = "inviter@email"
            )
        ) = AcceptInviteState.ItemInvite(
            progress = progress,
            event = event,
            invite = invite
        )

    }

    internal object Vault {

        internal fun create(
            progress: AcceptInviteProgress = AcceptInviteProgress.Pending,
            event: AcceptInviteEvent = AcceptInviteEvent.Idle,
            invite: AcceptInviteUiModel.Vault = AcceptInviteUiModel.Vault.User(
                inviterEmail = "inviter@email",
                name = "invite-name",
                itemCount = 1,
                memberCount = 1,
                icon = ShareIcon.Icon1,
                color = ShareColor.Color1
            )
        ) = AcceptInviteState.VaultInvite(
            progress = progress,
            event = event,
            invite = invite
        )

    }

}
