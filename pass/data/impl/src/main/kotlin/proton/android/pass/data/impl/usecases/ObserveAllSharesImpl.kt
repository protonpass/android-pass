package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.Result
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.pass.domain.Share
import javax.inject.Inject

class ObserveAllSharesImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : ObserveAllShares {

    override fun invoke(userId: UserId?): Flow<Result<List<Share>>> =
        if (userId == null) {
            accountManager.getPrimaryUserId()
                .flatMapLatest { primaryUserId ->
                    if (primaryUserId != null) {
                        shareRepository.observeAllShares(primaryUserId)
                    } else {
                        flowOf(Result.Error(UserIdNotAvailableError()))
                    }
                }
        } else {
            shareRepository.observeAllShares(userId)
        }
}
