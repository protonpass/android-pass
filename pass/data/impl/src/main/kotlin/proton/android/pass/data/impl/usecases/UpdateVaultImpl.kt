package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.SessionUserId
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.UpdateVault
import proton.pass.domain.Share
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

class UpdateVaultImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : UpdateVault {
    override suspend fun invoke(userId: SessionUserId?, shareId: ShareId, vault: NewVault): Share =
        if (userId == null) {
            val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
            if (primaryUserId != null) {
                performUpdate(primaryUserId, shareId, vault)
            } else {
                throw UserIdNotAvailableError()
            }
        } else {
            performUpdate(userId, shareId, vault)
        }

    private suspend fun performUpdate(
        userId: SessionUserId,
        shareId: ShareId,
        vault: NewVault
    ): Share = shareRepository.updateVault(userId, shareId, vault)
}
