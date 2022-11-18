package me.proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import me.proton.android.pass.data.api.usecases.GetCurrentShare
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import javax.inject.Inject

class GetCurrentShareImpl @Inject constructor(
    private val sharesRepository: me.proton.android.pass.data.api.repositories.ShareRepository
) : GetCurrentShare {
    override suspend operator fun invoke(userId: UserId): Result<List<Share>> =
        sharesRepository.observeShares(userId)
            .filterNotNull()
            .firstOrNull()
            ?: Result.Error()
}

