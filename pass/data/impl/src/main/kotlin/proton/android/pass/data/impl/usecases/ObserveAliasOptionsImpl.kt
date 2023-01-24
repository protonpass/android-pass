package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.AliasRepository
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.pass.domain.AliasOptions
import proton.pass.domain.ShareId
import javax.inject.Inject

class ObserveAliasOptionsImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val aliasRepository: AliasRepository
) : ObserveAliasOptions {

    override operator fun invoke(shareId: ShareId): Flow<AliasOptions> =
        accountManager.getPrimaryUserId()
            .filterNotNull()
            .flatMapLatest { getAliasOptions(it, shareId) }

    private fun getAliasOptions(userId: UserId, shareId: ShareId): Flow<AliasOptions> =
        aliasRepository.getAliasOptions(userId, shareId)
}
