package me.proton.core.pass.domain.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.repositories.ShareRepository
import javax.inject.Inject

class RefreshShares @Inject constructor(
    private val sharesRepository: ShareRepository
) {
    suspend operator fun invoke(userId: UserId) = sharesRepository.refreshShares(userId)
}
