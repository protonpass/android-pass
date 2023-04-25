package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.extensions.toVault
import proton.android.pass.data.api.errors.ShareContentNotAvailableError
import proton.android.pass.data.api.usecases.GetShareById
import proton.android.pass.data.api.usecases.GetVaultById
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import javax.inject.Inject

class GetVaultByIdImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val getShareById: GetShareById
) : GetVaultById {

    override fun invoke(userId: UserId?, shareId: ShareId): Flow<Vault> = flow {
        val share = getShareById(userId, shareId)
        when (val asVault = share.toVault(encryptionContextProvider)) {
            None -> throw ShareContentNotAvailableError()
            is Some -> emit(asVault.value)
        }
    }
}
