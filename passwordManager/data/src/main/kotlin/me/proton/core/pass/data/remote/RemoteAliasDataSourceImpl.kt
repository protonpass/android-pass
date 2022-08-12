package me.proton.core.pass.data.remote

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.pass.data.api.PasswordManagerApi
import me.proton.core.pass.data.responses.AliasDetails
import me.proton.core.pass.data.responses.AliasOptionsResponse
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ShareId

class RemoteAliasDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteAliasDataSource {
    override suspend fun getAliasOptions(userId: UserId, shareId: ShareId): AliasOptionsResponse =
        api.get<PasswordManagerApi>(userId).invoke {
            getAliasOptions(shareId.id)
        }.valueOrThrow.options

    override suspend fun getAliasDetails(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): AliasDetails =
        api.get<PasswordManagerApi>(userId).invoke {
            getAliasDetails(shareId.id, itemId.id)
        }.valueOrThrow.alias
}
