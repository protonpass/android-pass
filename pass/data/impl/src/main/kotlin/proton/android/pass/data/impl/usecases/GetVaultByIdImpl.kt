package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.errors.ShareContentNotAvailableError
import proton.android.pass.data.api.errors.ShareNotAvailableError
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import javax.inject.Inject

class GetVaultByIdImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val getShareById: GetShareById
) : GetVaultById {

    override suspend fun invoke(userId: UserId?, shareId: ShareId): Flow<Vault> = flow {
        when (val share = getShareById(userId, shareId)) {
            LoadingResult.Loading -> {}
            is LoadingResult.Error -> {
                PassLogger.w(TAG, share.exception, "Error getting share by id")
                throw share.exception
            }
            is LoadingResult.Success -> {
                val asVault = share.data?.toVault(encryptionContextProvider)
                    ?: throw ShareNotAvailableError()
                when (asVault) {
                    None -> throw ShareContentNotAvailableError()
                    is Some -> emit(asVault.value)
                }
            }
        }
    }

    companion object {
        private const val TAG = "GetVaultByIdImpl"
    }

}
