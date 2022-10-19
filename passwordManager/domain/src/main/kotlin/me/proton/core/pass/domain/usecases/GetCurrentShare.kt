package me.proton.core.pass.domain.usecases

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.repositories.ShareRepository
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
