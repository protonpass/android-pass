package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import proton.android.pass.data.api.usecases.GetCurrentUserId
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.toLoadingResult
import javax.inject.Inject

class GetCurrentUserIdImpl @Inject constructor(
    private val accountManager: AccountManager
) : GetCurrentUserId {
    override suspend operator fun invoke(): LoadingResult<UserId> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .firstOrNull()
        .toLoadingResult()
}

