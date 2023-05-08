package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.GetItemByAliasEmail
import proton.pass.domain.Item
import javax.inject.Inject

class GetItemByAliasEmailImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository
) : GetItemByAliasEmail {
    override suspend fun invoke(userId: UserId?, aliasEmail: String): Item? {
        val id = userId ?: requireNotNull(accountManager.getPrimaryUserId().first())
        return itemRepository.getItemByAliasEmail(id, aliasEmail)
    }
}
