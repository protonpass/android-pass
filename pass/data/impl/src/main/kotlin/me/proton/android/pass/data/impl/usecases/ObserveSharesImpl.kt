package me.proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import me.proton.android.pass.data.api.repositories.ShareRepository
import me.proton.android.pass.data.api.usecases.ObserveShares
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import javax.inject.Inject

class ObserveSharesImpl @Inject constructor(
    private val sharesRepository: ShareRepository
) : ObserveShares {

    override fun invoke(userId: UserId): Flow<Result<List<Share>>> =
        sharesRepository.observeShares(userId)
}
