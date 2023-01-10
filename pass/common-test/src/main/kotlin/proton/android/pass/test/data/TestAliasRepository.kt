package proton.android.pass.test.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import proton.android.pass.data.api.repositories.AliasRepository
import me.proton.core.domain.entity.UserId
import proton.pass.domain.AliasDetails
import proton.pass.domain.AliasMailbox
import proton.pass.domain.AliasOptions
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId

class TestAliasRepository : AliasRepository {

    private var aliasOptions: AliasOptions? = null
    private var aliasDetails: AliasDetails? = null

    fun setAliasOptions(aliasOptions: AliasOptions) {
        this.aliasOptions = aliasOptions
    }

    fun setAliasDetails(aliasDetails: AliasDetails) {
        this.aliasDetails = aliasDetails
    }

    override fun getAliasOptions(
        userId: UserId,
        shareId: ShareId
    ): Flow<AliasOptions> = flow {
        val currentValue = aliasOptions
        if (currentValue != null) {
            emit(currentValue)
        } else {
            throw IllegalStateException("Requested getAliasOptions before it was set")
        }
    }

    override fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<AliasDetails> = flow {
        val currentValue = aliasDetails
        if (currentValue != null) {
            emit(currentValue)
        } else {
            throw IllegalStateException("Requested getAliasDetails before it was set")
        }
    }

    override fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: List<AliasMailbox>
    ): Flow<Unit> = flow {
        emit(Unit)
    }

}
