package me.proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId

interface GetShareById {
    suspend operator fun invoke(userId: UserId, shareId: ShareId): Result<Share?>
}
