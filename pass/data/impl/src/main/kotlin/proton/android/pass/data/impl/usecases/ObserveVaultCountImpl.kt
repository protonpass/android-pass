package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.ObserveVaultCount
import javax.inject.Inject

class ObserveVaultCountImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
): ObserveVaultCount {

    override fun invoke(userId: UserId?): Flow<Int> = flow {
        val id = userId ?: requireNotNull(accountManager.getPrimaryUserId().first())
        emit(id)
    }.flatMapLatest {
        shareRepository.observeVaultCount(it)
    }
}
