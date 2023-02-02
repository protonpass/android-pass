package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Share
import proton.pass.domain.ShareId

interface GetShareById {
    suspend operator fun invoke(userId: UserId, shareId: ShareId): LoadingResult<Share?>
}
