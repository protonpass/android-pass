package me.proton.pass.domain.repositories

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

interface AliasRepository {
    suspend fun getAliasOptions(userId: UserId, shareId: ShareId): Result<AliasOptions>
    suspend fun getAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<List<AliasMailbox>>
}
