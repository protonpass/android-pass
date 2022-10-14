package me.proton.core.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.common.api.map
import me.proton.core.pass.common.api.toResult
import me.proton.core.pass.data.api.PasswordManagerApi
import me.proton.core.pass.data.responses.AliasDetails
import me.proton.core.pass.data.responses.AliasOptionsResponse
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId
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
