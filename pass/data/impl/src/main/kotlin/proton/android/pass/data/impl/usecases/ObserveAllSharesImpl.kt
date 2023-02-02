package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.pass.domain.Share
import javax.inject.Inject

class ObserveAllSharesImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val shareRepository: ShareRepository
) : ObserveAllShares {

    override fun invoke(userId: UserId?): Flow<LoadingResult<List<Share>>> =
        if (userId == null) {
            observeCurrentUser()
                .flatMapLatest {
                    shareRepository.observeAllShares(it.userId)
                }
        } else {
            shareRepository.observeAllShares(userId)
        }
}
