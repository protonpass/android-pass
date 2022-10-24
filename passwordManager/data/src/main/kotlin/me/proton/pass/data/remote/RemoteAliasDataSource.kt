package me.proton.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.data.responses.AliasDetails
import me.proton.pass.data.responses.AliasOptionsResponse
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

interface RemoteAliasDataSource {
    suspend fun getAliasOptions(userId: UserId, shareId: ShareId): Result<AliasOptionsResponse>
    suspend fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<AliasDetails>
}
