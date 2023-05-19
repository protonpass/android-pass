package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.impl.local.LocalItemDataSource
import javax.inject.Inject

class ObserveMFACountImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val localItemDataSource: LocalItemDataSource
) : ObserveMFACount {
    override fun invoke(): Flow<Int> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .flatMapLatest {
            localItemDataSource.observeAllItemsWithTotp(it)
        }
        .map { it.size }
}
