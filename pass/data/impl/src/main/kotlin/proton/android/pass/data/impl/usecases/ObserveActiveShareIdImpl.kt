package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.flatMap
import proton.android.pass.common.api.map
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.pass.domain.ShareId
import javax.inject.Inject

class ObserveActiveShareIdImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : ObserveActiveShare {

    override operator fun invoke(userId: UserId?): Flow<LoadingResult<ShareId>> =
        if (userId == null) {
            accountManager.getPrimaryUserId()
                .filterNotNull()
                .flatMapLatest(::observeSelectedShare)
        } else {
            observeSelectedShare(userId)
        }

    private fun observeSelectedShare(userId: UserId): Flow<LoadingResult<ShareId>> =
        shareRepository.observeSelectedShares(userId)
            .map { result ->
                result.flatMap { list ->
                    val selectedShare = list.firstOrNull()
                    if (selectedShare != null) {
                        LoadingResult.Success(selectedShare.id)
                    } else {
                        onNoShareSelectedSelectFirstShare(userId)
                    }
                }
            }

    private suspend fun onNoShareSelectedSelectFirstShare(userId: UserId) =
        shareRepository.observeAllShares(userId)
            .map {
                it.flatMap { list ->
                    if (list.isNotEmpty()) {
                        shareRepository.selectVault(userId, list.first().id)
                            .map { list.first().id }
                    } else {
                        LoadingResult.Loading
                    }
                }
            }
            .first()
}
