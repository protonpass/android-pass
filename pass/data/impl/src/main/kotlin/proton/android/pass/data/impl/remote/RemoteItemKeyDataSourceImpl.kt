package proton.android.pass.data.impl.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.responses.ItemLatestKeyResponse
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteItemKeyDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteItemKeyDataSource {
    override suspend fun fetchLatestItemKey(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): ItemLatestKeyResponse =
        api.get<PasswordManagerApi>(userId).invoke {
            getItemLatestKey(shareId.id, itemId.id)
        }.valueOrThrow.key
}
