package me.proton.core.pass.domain.repositories

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.AliasMailbox
import me.proton.core.pass.domain.AliasOptions
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

interface AliasRepository {
    suspend fun getAliasOptions(userId: UserId, shareId: ShareId): AliasOptions
    suspend fun getAliasMailboxes(userId: UserId, shareId: ShareId, itemId: ItemId): List<AliasMailbox>
}
