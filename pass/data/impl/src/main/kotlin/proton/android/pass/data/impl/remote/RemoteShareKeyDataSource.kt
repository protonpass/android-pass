package proton.android.pass.data.impl.remote

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.responses.ShareKeyResponse
import proton.pass.domain.ShareId

interface RemoteShareKeyDataSource {
    fun getShareKeys(userId: UserId, shareId: ShareId): Flow<List<ShareKeyResponse>>
}
