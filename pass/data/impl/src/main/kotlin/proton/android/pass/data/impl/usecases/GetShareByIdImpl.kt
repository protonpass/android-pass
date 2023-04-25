package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.GetShareById
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import javax.inject.Inject

class GetShareByIdImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : GetShareById {

    override suspend fun invoke(userId: UserId?, shareId: ShareId): Share =
        if (userId == null) {
            val primaryAccount = requireNotNull(accountManager.getPrimaryAccount().firstOrNull())
            getShare(primaryAccount.userId, shareId)
        } else {
            getShare(userId, shareId)
        }

    private suspend fun getShare(userId: UserId, shareId: ShareId): Share =
        shareRepository.getById(userId, shareId)
}

