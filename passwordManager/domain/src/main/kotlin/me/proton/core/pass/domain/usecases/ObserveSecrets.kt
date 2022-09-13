package me.proton.core.pass.domain.usecases

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.commonsecret.Secret
import me.proton.core.pass.domain.repositories.SecretsRepository

class ObserveSecrets @Inject constructor(
    private val repository: SecretsRepository
) {
    // TODO: allow pagination
    operator fun invoke(userId: UserId): Flow<List<Secret>> {
        return repository.observeSecrets(userId)
    }
}
