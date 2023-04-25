package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.DeleteVault
import proton.pass.domain.ShareId
import javax.inject.Inject

class DeleteVaultImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : DeleteVault {

    override suspend fun invoke(shareId: ShareId) {
        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        shareRepository.deleteVault(userId, shareId)
    }
}
