package me.proton.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.map
import me.proton.pass.common.api.toResult
import me.proton.pass.data.api.PasswordManagerApi
import me.proton.pass.data.responses.AliasDetails
import me.proton.pass.data.responses.AliasOptionsResponse
import me.proton.pass.domain.ItemId
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteAliasDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteAliasDataSource {
    override suspend fun getAliasOptions(
        userId: UserId,
        shareId: ShareId
    ): Result<AliasOptionsResponse> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                getAliasOptions(shareId.id)
            }
            .toResult()
            .map { it.options }

    override suspend fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Result<AliasDetails> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                getAliasDetails(shareId.id, itemId.id)
            }
            .toResult()
            .map { it.alias }
}
