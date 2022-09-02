package me.proton.core.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.data.responses.AliasDetails
import me.proton.core.pass.data.responses.AliasOptionsResponse
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

interface RemoteAliasDataSource {
    suspend fun getAliasOptions(userId: UserId, shareId: ShareId): AliasOptionsResponse
    suspend fun getAliasDetails(userId: UserId, shareId: ShareId, itemId: ItemId): AliasDetails
}
