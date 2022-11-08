package me.proton.pass.test.data

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.AliasDetails
import me.proton.pass.domain.AliasMailbox
import me.proton.pass.domain.AliasOptions
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.repositories.AliasRepository

class TestAliasRepository : AliasRepository {

    private var aliasOptions: Result<AliasOptions> = Result.Loading
    private var aliasDetails: Result<AliasDetails> = Result.Loading
    private var updateAliasMailboxesResult: Result<Unit> = Result.Loading

    fun setAliasOptions(aliasOptions: Result<AliasOptions>) {
        this.aliasOptions = aliasOptions
    }

    fun setAliasDetails(aliasDetails: Result<AliasDetails>) {
        this.aliasDetails = aliasDetails
    }

    fun setUpdateAliasMailboxesResult(result: Result<Unit>) {
        this.updateAliasMailboxesResult = result
    }

    override suspend fun getAliasOptions(
        userId: UserId,
        shareId: ShareId
    ): Result<AliasOptions> = aliasOptions

    override suspend fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<AliasDetails> = aliasDetails

    override suspend fun updateAliasMailboxes(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId,
        mailboxes: List<AliasMailbox>
    ): Result<Unit> = updateAliasMailboxesResult

}
