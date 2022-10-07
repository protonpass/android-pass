package me.proton.core.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.repositories.ShareRepository
import javax.inject.Inject

class ObserveShares @Inject constructor(
    private val sharesRepository: ShareRepository
) {
    operator fun invoke(userId: UserId): Flow<List<Share>> = sharesRepository.observeShares(userId)
}
