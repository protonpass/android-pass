package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.pass.domain.Share
import proton.pass.domain.ShareId

interface GetShareById {
    suspend operator fun invoke(userId: UserId? = null, shareId: ShareId): Share
}
