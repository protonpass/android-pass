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
        internal val invite: AcceptInviteUiModel.Item
    ) : AcceptInviteState

    @Stable
    data class VaultInvite(
        override val progress: AcceptInviteProgress,
        override val event: AcceptInviteEvent,
        internal val invite: AcceptInviteUiModel.Vault
    ) : AcceptInviteState

}

@Stable
internal sealed interface AcceptInviteUiModel {

    val inviterEmail: String

    @Stable
    sealed interface Item : AcceptInviteUiModel {

        @Stable
        data class User(
            override val inviterEmail: String
        ) : Item

        @Stable
        data class Group(
            override val inviterEmail: String,
            val groupName: String
        ) : Item
    }

    @Stable
    sealed interface Vault : AcceptInviteUiModel {

        val name: String
        val itemCount: Int
        val memberCount: Int
        val icon: ShareIcon
        val color: ShareColor

        @Stable
        data class User(
            override val inviterEmail: String,
            override val name: String,
            override val itemCount: Int,
            override val memberCount: Int,
            override val icon: ShareIcon,
            override val color: ShareColor
        ) : Vault

        @Stable
        data class Group(
            override val inviterEmail: String,
            val groupName: String,
            override val name: String,
            override val itemCount: Int,
            override val memberCount: Int,
            override val icon: ShareIcon,
            override val color: ShareColor
        ) : Vault
    }
}
