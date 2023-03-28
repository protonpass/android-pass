package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.MarkVaultAsPrimary
import proton.pass.domain.ShareId
import javax.inject.Inject

class MarkVaultAsPrimaryImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : MarkVaultAsPrimary {

    override suspend fun invoke(userId: UserId?, shareId: ShareId) {
        val id = userId ?: requireNotNull(accountManager.getPrimaryUserId().first())
        shareRepository.markAsPrimary(id, shareId)
    }

}
