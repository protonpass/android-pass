package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.MigrateVault
import proton.pass.domain.ShareId
import javax.inject.Inject

class MigrateVaultImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository
) : MigrateVault {

    override suspend fun invoke(origin: ShareId, dest: ShareId) {
        val userId = requireNotNull(accountManager.getPrimaryUserId().firstOrNull())
        itemRepository.migrateItems(userId = userId, source = origin, destination = dest)
    }
}
