package me.proton.core.pass.domain.usecases

import me.proton.core.pass.domain.repositories.SecretsRepository
import javax.inject.Inject

class DeleteSecret @Inject constructor(
    private val secretsRepository: SecretsRepository
) {
    suspend operator fun invoke(secretId: String): Boolean = secretsRepository.delete(secretId)
}
