package proton.android.pass.data.impl.usecases

import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.RefreshShares
import me.proton.core.domain.entity.UserId
import javax.inject.Inject

class RefreshSharesImpl @Inject constructor(
    private val sharesRepository: ShareRepository
) : RefreshShares {
    override suspend fun invoke(userId: UserId) = sharesRepository.refreshShares(userId)
}

