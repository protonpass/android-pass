package me.proton.core.pass.domain.usecases

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.entity.NewVault
import me.proton.core.pass.domain.repositories.ShareRepository
import javax.inject.Inject

class CreateVault @Inject constructor(
    private val shareRepository: ShareRepository
) {
    suspend operator fun invoke(userId: SessionUserId, vault: NewVault): Result<Share> =
        shareRepository.createVault(userId, vault)
}
