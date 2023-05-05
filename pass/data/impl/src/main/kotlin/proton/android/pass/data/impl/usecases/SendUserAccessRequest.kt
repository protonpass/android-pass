package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.network.data.ApiProvider
import me.proton.core.network.domain.ApiException
import me.proton.core.network.domain.isRetryable
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.local.LocalPlanLimitsDataSource
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

sealed interface SendUserAccessResult {
    object Success : SendUserAccessResult
    object Failure : SendUserAccessResult
    object Retry : SendUserAccessResult
}

interface SendUserAccessRequest {
    suspend operator fun invoke(): SendUserAccessResult
}

class SendUserAccessRequestImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val api: ApiProvider,
    private val localPlanLimitsDataSource: LocalPlanLimitsDataSource
) : SendUserAccessRequest {
    override suspend fun invoke(): SendUserAccessResult {
        val account = accountManager.getPrimaryAccount().firstOrNull()
        if (account == null) {
            PassLogger.w(TAG, "Error getting primary account")
            return SendUserAccessResult.Failure
        }

        return runCatching {
            api.get<PasswordManagerApi>(account.userId)
                .invoke { userAccess() }
                .valueOrThrow
                .also {
                    localPlanLimitsDataSource.storePlanLimits(
                        userId = account.userId,
                        vaultLimit = it.accessResponse.planResponse.vaultLimit ?: -1,
                        aliasLimit = it.accessResponse.planResponse.aliasLimit ?: -1,
                        totpLimit = it.accessResponse.planResponse.totpLimit ?: -1
                    )
                }
        }.fold(
            onSuccess = {
                PassLogger.i(TAG, "Successfully sent userAccess")
                SendUserAccessResult.Success
            },
            onFailure = {
                PassLogger.w(TAG, it, "ApiException when sending user request")
                if (it is ApiException && it.isRetryable()) {
                    SendUserAccessResult.Retry
                } else {
                    SendUserAccessResult.Failure
                }
            }
        )
    }

    companion object {
        private const val TAG = "SendUserAccessRequestImpl"
    }
}
