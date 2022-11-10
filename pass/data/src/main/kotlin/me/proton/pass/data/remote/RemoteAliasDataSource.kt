package me.proton.pass.data.remote

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.pass.data.requests.UpdateAliasMailboxesRequest
import me.proton.pass.data.responses.AliasDetails
import me.proton.pass.data.responses.AliasOptionsResponse
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

interface RemoteAliasDataSource {
    fun getAliasOptions(userId: UserId, shareId: ShareId): Flow<AliasOptionsResponse>
    fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AliasDetails>
    fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: UpdateAliasMailboxesRequest
    ): Flow<AliasDetails>
}
