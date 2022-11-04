package me.proton.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.repositories.ShareRepository
import javax.inject.Inject

class ObserveShares @Inject constructor(
    private val sharesRepository: ShareRepository
) {
    operator fun invoke(userId: UserId): Flow<Result<List<Share>>> =
        sharesRepository.observeShares(userId)
}
