package me.proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.android.pass.data.api.repositories.AliasRepository
import me.proton.core.domain.entity.UserId
import me.proton.pass.domain.AliasDetails
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class TestAliasRepository @Inject constructor() : AliasRepository {
    override fun getAliasOptions(userId: UserId, shareId: ShareId): Flow<AliasOptions> {
        TODO("Not yet implemented")
    }

    override fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AliasDetails> {
        TODO("Not yet implemented")
    }

    override fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: List<AliasMailbox>
    ): Flow<Unit> {
        TODO("Not yet implemented")
    }
}
