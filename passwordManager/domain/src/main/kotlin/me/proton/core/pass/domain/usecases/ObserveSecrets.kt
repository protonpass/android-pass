package me.proton.core.pass.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common_secret.Secret
import me.proton.core.pass.domain.repositories.SecretsRepository
import me.proton.core.user.domain.UserAddressManager
import javax.inject.Inject

class ObserveSecrets @Inject constructor(
    private val addressManager: UserAddressManager,
    private val repository: SecretsRepository,
) {
    // TODO: allow pagination
    operator fun invoke(userId: UserId): Flow<List<Secret>> {
        return repository.observeSecrets(userId)
    }
}
