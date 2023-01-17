package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.Result
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.UpdateActiveShare
import proton.pass.domain.ShareId
import javax.inject.Inject

class UpdateActiveShareImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : UpdateActiveShare {

    override suspend fun invoke(shareId: ShareId): Result<Unit> =
        accountManager.getPrimaryUserId()
            .map { userId ->
                if (userId != null) {
                    shareRepository.selectVault(userId, shareId)
                } else {
                    Result.Error(UserIdNotAvailableError())
                }
            }
            .first()
}
