package me.proton.core.pass.domain.usecases

import javax.inject.Inject
import me.proton.core.pass.commonsecret.Secret
import me.proton.core.pass.domain.repositories.SecretsRepository
import me.proton.core.user.domain.entity.UserAddress

class SearchSecretWithName @Inject constructor(
    private val repository: SecretsRepository
) {
    suspend operator fun invoke(address: UserAddress?, name: String): List<Secret> =
        repository.searchWithName(address, name)
}
