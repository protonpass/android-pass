package me.proton.pass.domain.usecases

import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.repositories.ShareRepository
import javax.inject.Inject

class GetShareById @Inject constructor(
    private val shareRepository: ShareRepository
) {
    suspend operator fun invoke(userId: UserId, shareId: ShareId): Result<Share?> =
        shareRepository.getById(userId, shareId)
}
