package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.CreateVault
import proton.pass.domain.Share
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

class CreateVaultImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : CreateVault {

    override suspend fun invoke(userId: SessionUserId?, vault: NewVault): Share =
        if (userId == null) {
            val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
            if (primaryUserId != null) {
                createVault(primaryUserId, vault)
            } else {
                throw UserIdNotAvailableError()
            }
        } else {
            createVault(userId, vault)
        }


    private suspend fun createVault(userId: UserId, vault: NewVault): Share =
        shareRepository.createVault(userId, vault)
}
