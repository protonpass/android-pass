package me.proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import me.proton.android.pass.data.impl.requests.UpdateAliasMailboxesRequest
import me.proton.android.pass.data.impl.responses.AliasDetails
import me.proton.android.pass.data.impl.responses.AliasOptionsResponse
import me.proton.core.domain.entity.UserId
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
