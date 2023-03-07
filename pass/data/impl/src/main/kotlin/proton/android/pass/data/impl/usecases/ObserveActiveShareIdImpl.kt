package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.map
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.pass.domain.Share
import javax.inject.Inject

class ObserveActiveShareIdImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : ObserveActiveShare {

    override operator fun invoke(userId: UserId?): Flow<Share> =
        if (userId == null) {
            accountManager.getPrimaryUserId()
                .filterNotNull()
                .flatMapLatest(::observeSelectedShare)
        } else {
            observeSelectedShare(userId)
        }

    private fun observeSelectedShare(userId: UserId): Flow<Share> = flow {
        shareRepository.observeSelectedShares(userId)
            .collect { result ->
                if (result is LoadingResult.Success) {
                    val selectedShare = getSelectedShare(userId, result.data).first()
                    emit(selectedShare)
                }
            }
    }

    private fun getSelectedShare(userId: UserId, shares: List<Share>): Flow<Share> = flow {
        val selectedShare = shares.firstOrNull()
            ?: onNoShareSelectedSelectFirstShare(userId).first()
        emit(selectedShare)
    }

    private fun onNoShareSelectedSelectFirstShare(userId: UserId): Flow<Share> = flow {
        shareRepository.observeAllShares(userId)
            .collect { result ->
                if (result is LoadingResult.Success) {
                    val shares = result.data
                    if (shares.isNotEmpty()) {
                        val firstShare = shares.first()
                        shareRepository.selectVault(userId, firstShare.id)
                            .map { emit(firstShare) }
                    } else {
                        throw IllegalStateException("Could not find any share for user")
                    }
                }
            }
    }
}
