package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.remote.RemoteDataSourceConstants.PAGE_SIZE
import proton.android.pass.data.impl.responses.ShareKeyResponse
import proton.pass.domain.ShareId
import javax.inject.Inject

class RemoteShareKeyDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : RemoteShareKeyDataSource {

    override fun getShareKeys(userId: UserId, shareId: ShareId): Flow<List<ShareKeyResponse>> = flow {
        val res = api.get<PasswordManagerApi>(userId)
            .invoke {
                var page = 0
                val shareKeys = mutableListOf<ShareKeyResponse>()

                while (true) {
                    val pageKeys = getShareKeys(shareId.id, page, PAGE_SIZE)
                    shareKeys.addAll(pageKeys.keys.keys)

                    if (pageKeys.keys.keys.size < PAGE_SIZE) {
                        break
                    } else {
                        page++
                    }
                }
                shareKeys
            }
            .valueOrThrow
        emit(res)
    }
}
