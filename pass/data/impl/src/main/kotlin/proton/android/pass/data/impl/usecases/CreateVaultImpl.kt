package proton.android.pass.data.impl.usecases

import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.CreateVault
import me.proton.core.domain.entity.SessionUserId
import proton.android.pass.common.api.Result
import proton.pass.domain.Share
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

class CreateVaultImpl @Inject constructor(
    private val shareRepository: ShareRepository
) : CreateVault {

    override suspend fun invoke(userId: SessionUserId, vault: NewVault): Result<Share> =
        shareRepository.createVault(userId, vault)
}
