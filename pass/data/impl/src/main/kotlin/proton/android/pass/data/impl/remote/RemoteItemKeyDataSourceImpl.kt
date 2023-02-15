package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    override fun getLatestItemKey(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<ItemLatestKeyResponse> =
        flow {
            val res = api.get<PasswordManagerApi>(userId).invoke {
                getItemLatestKey(shareId.id, itemId.id)
            }.valueOrThrow
                .key
            emit(res)
        }
}
