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

package proton.android.pass.data.impl.extensions

import proton.android.pass.data.impl.responses.SyncEventInvitesChangedApiModel
import proton.android.pass.data.impl.responses.SyncEventShareApiModel
import proton.android.pass.data.impl.responses.SyncEventShareFolderApiModel
import proton.android.pass.data.impl.responses.SyncEventShareItemApiModel
import proton.android.pass.data.impl.responses.UserEventListApiModel
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.UserEventId
import proton.android.pass.domain.events.EventToken
import proton.android.pass.domain.events.SyncEventInvitesChanged
import proton.android.pass.domain.events.SyncEventShare
import proton.android.pass.domain.events.SyncEventShareFolder
import proton.android.pass.domain.events.SyncEventShareItem
import proton.android.pass.domain.events.UserEventList

fun SyncEventShareItemApiModel.toDomain(): SyncEventShareItem = SyncEventShareItem(
    shareId = ShareId(shareId),
    itemId = ItemId(itemId),
    eventToken = EventToken(eventToken)
)

fun SyncEventShareApiModel.toDomain(): SyncEventShare = SyncEventShare(
    shareId = ShareId(shareId),
    eventToken = EventToken(eventToken)
)

fun SyncEventShareFolderApiModel.toDomain(): SyncEventShareFolder = SyncEventShareFolder(
    shareId = ShareId(shareId),
    folderId = FolderId(folderId),
    eventToken = EventToken(eventToken)
)

fun SyncEventInvitesChangedApiModel.toDomain(): SyncEventInvitesChanged = SyncEventInvitesChanged(
    eventToken = EventToken(eventToken)
)

fun UserEventListApiModel.toDomain(): UserEventList = UserEventList(
    lastEventId = UserEventId(lastEventId),
    itemsUpdated = itemsUpdated.map { it.toDomain() },
    itemsDeleted = itemsDeleted.map { it.toDomain() },
    aliasNoteChanged = aliasNoteChanged.map { it.toDomain() },
    sharesCreated = sharesCreated.map { it.toDomain() },
    sharesUpdated = sharesUpdated.map { it.toDomain() },
    sharesDeleted = sharesDeleted.map { it.toDomain() },
    foldersUpdated = foldersUpdated.map { it.toDomain() },
    foldersDeleted = foldersDeleted.map { it.toDomain() },
    invitesChanged = invitesChanged?.toDomain(),
    groupInvitesChanged = groupInvitesChanged?.toDomain(),
    pendingAliasToCreateChanged = pendingAliasToCreateChanged?.toDomain(),
    breachUpdate = breachUpdate?.toDomain(),
    organizationInfoChanged = organizationInfoChanged?.toDomain(),
    sharesWithInvitesToCreate = sharesWithInvitesToCreate.map { it.toDomain() },
    refreshUser = refreshUser,
    eventsPending = eventsPending,
    fullRefresh = fullRefresh
)
