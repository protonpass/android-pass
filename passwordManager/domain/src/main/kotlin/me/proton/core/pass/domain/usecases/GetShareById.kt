package me.proton.core.pass.domain.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.repositories.ShareRepository
import javax.inject.Inject

class GetShareById @Inject constructor(
    private val shareRepository: ShareRepository
) {
    suspend operator fun invoke(userId: UserId,  shareId: ShareId): Share? =
        shareRepository.getById(userId, shareId)
}