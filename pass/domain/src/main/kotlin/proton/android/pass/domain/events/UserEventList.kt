/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.domain.events

import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserEventId

data class SyncEventShareItem(
    val shareId: ShareId,
    val itemId: ItemId,
    val eventToken: EventToken
)

data class SyncEventShare(
    val shareId: ShareId,
    val eventToken: EventToken
)

data class SyncEventShareFolder(
    val shareId: ShareId,
    val folderId: FolderId,
    val eventToken: EventToken
)

data class SyncEventInvitesChanged(
    val eventToken: EventToken
)

data class UserEventList(
    val lastEventId: UserEventId,
    val itemsUpdated: List<SyncEventShareItem>,
    val itemsDeleted: List<SyncEventShareItem>,
    val aliasNoteChanged: List<SyncEventShareItem>,
    val sharesCreated: List<SyncEventShare>,
    val sharesUpdated: List<SyncEventShare>,
    val sharesDeleted: List<SyncEventShare>,
    val foldersUpdated: List<SyncEventShareFolder>,
    val foldersDeleted: List<SyncEventShareFolder>,
    val invitesChanged: SyncEventInvitesChanged?,
    val groupInvitesChanged: SyncEventInvitesChanged?,
    val pendingAliasToCreateChanged: SyncEventInvitesChanged?,
    val breachUpdate: SyncEventInvitesChanged?,
    val sharesWithInvitesToCreate: List<SyncEventShare>,
    val refreshUser: Boolean,
    val eventsPending: Boolean,
    val fullRefresh: Boolean
)
