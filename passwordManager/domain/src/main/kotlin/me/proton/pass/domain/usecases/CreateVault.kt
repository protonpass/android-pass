package me.proton.pass.domain.usecases

import me.proton.core.domain.entity.SessionUserId
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Share
import me.proton.pass.domain.entity.NewVault
import me.proton.pass.domain.repositories.ShareRepository
import javax.inject.Inject

class CreateVault @Inject constructor(
    private val shareRepository: ShareRepository
) {
    suspend operator fun invoke(userId: SessionUserId, vault: NewVault): Result<Share> =
        shareRepository.createVault(userId, vault)
}
