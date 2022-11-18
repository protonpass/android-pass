package me.proton.android.pass.data.api.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.pass.domain.AliasDetails
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId

interface AliasRepository {
    fun getAliasOptions(userId: UserId, shareId: ShareId): Flow<AliasOptions>
    fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AliasDetails>
    fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: List<AliasMailbox>
    ): Flow<Unit>
}
