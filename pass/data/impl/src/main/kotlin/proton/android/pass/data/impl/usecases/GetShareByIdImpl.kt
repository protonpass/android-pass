package proton.android.pass.data.impl.usecases

import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.GetShareById
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import javax.inject.Inject

class GetShareByIdImpl @Inject constructor(
    private val shareRepository: ShareRepository
) : GetShareById {

    override suspend fun invoke(userId: UserId, shareId: ShareId): LoadingResult<Share?> =
        shareRepository.getById(userId, shareId)
}

