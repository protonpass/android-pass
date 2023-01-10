package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import proton.android.pass.data.api.repositories.AliasRepository
import me.proton.core.domain.entity.UserId
import proton.pass.domain.AliasDetails
import proton.pass.domain.AliasMailbox
import proton.pass.domain.AliasOptions
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
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
