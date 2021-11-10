package me.proton.core.pass.domain.usecases

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.pass.common_secret.Secret
import me.proton.core.pass.domain.repositories.SecretsRepository
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.repository.UserAddressRepository
import javax.inject.Inject

class SearchSecretWithUri @Inject constructor(
    private val secretsRepository: SecretsRepository,
    private val userAddressRepository: UserAddressRepository,
) {
    suspend operator fun invoke(
        uri: String,
        userId: SessionUserId? = null,
        addressId: AddressId? = null,
    ): List<Secret> {
        val address = if (userId != null && addressId != null)
            userAddressRepository.getAddress(userId, addressId) else null
        return secretsRepository.searchWithUri(address, uri)
    }
}
