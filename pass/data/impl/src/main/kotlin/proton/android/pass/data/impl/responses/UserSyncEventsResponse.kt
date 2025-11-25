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

package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserSyncEventsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Events")
    val events: UserEventListApiModel
)

@Serializable
data class SyncEventShareItemApiModel(
    @SerialName("ShareID")
    val shareId: String,
    @SerialName("ItemID")
    val itemId: String,
    @SerialName("EventToken")
    val eventToken: String
)

@Serializable
data class SyncEventShareApiModel(
    @SerialName("ShareID")
    val shareId: String,
    @SerialName("EventToken")
    val eventToken: String
)

@Serializable
data class SyncEventShareFolderApiModel(
    @SerialName("ShareID")
    val shareId: String,
    @SerialName("FolderID")
    val folderId: String,
    @SerialName("EventToken")
    val eventToken: String
)

@Serializable
data class SyncEventInvitesChangedApiModel(
    @SerialName("EventToken")
    val eventToken: String
)

@Serializable
data class UserEventListApiModel(
    @SerialName("LastEventID")
    val lastEventId: String,
    @SerialName("ItemsUpdated")
    val itemsUpdated: List<SyncEventShareItemApiModel>,
    @SerialName("ItemsDeleted")
    val itemsDeleted: List<SyncEventShareItemApiModel>,
    @SerialName("AliasNoteChanged")
    val aliasNoteChanged: List<SyncEventShareItemApiModel>,
    @SerialName("SharesCreated")
    val sharesCreated: List<SyncEventShareApiModel>,
    @SerialName("SharesUpdated")
    val sharesUpdated: List<SyncEventShareApiModel>,
    @SerialName("SharesDeleted")
    val sharesDeleted: List<SyncEventShareApiModel>,
    @SerialName("FoldersUpdated")
    val foldersUpdated: List<SyncEventShareFolderApiModel>,
    @SerialName("FoldersDeleted")
    val foldersDeleted: List<SyncEventShareFolderApiModel>,
    @SerialName("InvitesChanged")
    val invitesChanged: SyncEventInvitesChangedApiModel? = null,
    @SerialName("GroupInvitesChanged")
    val groupInvitesChanged: SyncEventInvitesChangedApiModel? = null,
    @SerialName("PendingAliasToCreateChanged")
    val pendingAliasToCreateChanged: SyncEventInvitesChangedApiModel? = null,
    @SerialName("SharesWithInvitesToCreate")
    val sharesWithInvitesToCreate: List<SyncEventShareApiModel>,
    @SerialName("PlanChanged")
    val planChanged: Boolean,
    @SerialName("EventsPending")
    val eventsPending: Boolean,
    @SerialName("FullRefresh")
    val fullRefresh: Boolean
)
