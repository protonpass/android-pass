package me.proton.core.pass.domain.usecases

import javax.inject.Inject
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.entity.NewVault
import me.proton.core.pass.domain.repositories.ShareRepository

class CreateVault @Inject constructor(
    private val shareRepository: ShareRepository
) {
    suspend operator fun invoke(userId: SessionUserId, vault: NewVault): Share =
        shareRepository.createVault(userId, vault)
}
