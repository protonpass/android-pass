package me.proton.pass.domain.usecases

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.repositories.ShareRepository
import javax.inject.Inject

interface GetCurrentShare {
    suspend operator fun invoke(userId: UserId): Result<List<Share>>
}

class GetCurrentShareImpl @Inject constructor(
    private val sharesRepository: ShareRepository
) : GetCurrentShare {
    override suspend operator fun invoke(userId: UserId): Result<List<Share>> =
        sharesRepository.observeShares(userId)
            .filterNotNull()
            .firstOrNull()
            ?: Result.Error()
}
