package me.proton.android.pass.data.impl.usecases

import me.proton.android.pass.data.api.usecases.CreateVault
import me.proton.core.domain.entity.SessionUserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.entity.NewVault
import javax.inject.Inject

class CreateVaultImpl @Inject constructor(
    private val shareRepository: me.proton.android.pass.data.api.repositories.ShareRepository
) : CreateVault {

    override suspend fun invoke(userId: SessionUserId, vault: NewVault): Result<Share> =
        shareRepository.createVault(userId, vault)
}
