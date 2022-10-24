package me.proton.pass.domain.usecases

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.toResult
import javax.inject.Inject

interface GetCurrentUserId {
    suspend operator fun invoke(): Result<UserId>
}

class GetCurrentUserIdImpl @Inject constructor(
    private val accountManager: AccountManager
) : GetCurrentUserId {
    override suspend operator fun invoke(): Result<UserId> = accountManager.getPrimaryUserId()
        .filterNotNull()
        .firstOrNull()
        .toResult()
}
