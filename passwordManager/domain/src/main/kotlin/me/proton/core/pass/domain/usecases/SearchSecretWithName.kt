package me.proton.core.pass.domain.usecases

import me.proton.core.pass.common_secret.Secret
import me.proton.core.pass.domain.repositories.SecretsRepository
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

class SearchSecretWithName @Inject constructor(
    private val repository: SecretsRepository,
) {
   suspend operator fun invoke(address: UserAddress?, name: String): List<Secret> =
       repository.searchWithName(address, name)
}
