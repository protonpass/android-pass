package me.proton.android.pass.data.impl.usecases

import me.proton.android.pass.data.api.usecases.GetShareById
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import javax.inject.Inject

class GetShareByIdImpl @Inject constructor(
    private val shareRepository: me.proton.android.pass.data.api.repositories.ShareRepository
) : GetShareById {

    override suspend fun invoke(userId: UserId, shareId: ShareId): Result<Share?> =
        shareRepository.getById(userId, shareId)
}

