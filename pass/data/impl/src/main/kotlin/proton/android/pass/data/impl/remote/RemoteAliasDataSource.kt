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

package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.requests.ChangeAliasStatusRequest
import proton.android.pass.data.impl.requests.UpdateAliasMailboxesRequest
import proton.android.pass.data.impl.requests.alias.UpdateAliasNameRequest
import proton.android.pass.data.impl.requests.alias.UpdateAliasNoteRequest
import proton.android.pass.data.impl.responses.AliasOptionsResponse
import proton.android.pass.data.impl.responses.AliasResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.events.EventToken

interface RemoteAliasDataSource {

    fun getAliasOptions(userId: UserId, shareId: ShareId): Flow<AliasOptionsResponse>

    suspend fun fetchAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        eventToken: EventToken?
    ): AliasResponse

    fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: UpdateAliasMailboxesRequest
    ): Flow<AliasResponse>

    suspend fun changeAliasStatus(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        request: ChangeAliasStatusRequest
    )

    suspend fun updateAliasName(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        request: UpdateAliasNameRequest
    )

    suspend fun updateAliasNote(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        request: UpdateAliasNoteRequest
    )

}
