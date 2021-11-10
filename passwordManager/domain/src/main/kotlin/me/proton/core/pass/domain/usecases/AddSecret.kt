package me.proton.core.pass.domain.usecases

import me.proton.core.domain.entity.SessionUserId
import me.proton.core.pass.common_secret.SecretType
import me.proton.core.pass.common_secret.SecretValue
import me.proton.core.pass.common_secret.Secret
import me.proton.core.pass.domain.repositories.SecretsRepository
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.repository.UserAddressRepository
import java.util.UUID
import javax.inject.Inject

class AddSecret @Inject constructor(
    private val secretsRepository: SecretsRepository,
    private val userAddressRepository: UserAddressRepository,
) {
    suspend operator fun invoke(
        userId: SessionUserId,
        addressId: AddressId,
        name: String,
        type: SecretType,
        contents: SecretValue,
        associatedUri: String
    ) {
        val secret = Secret(
            // TODO: use a proper id here
            id = UUID.randomUUID().toString(),
            userId = userId.id,
            addressId = addressId.id,
            name = name,
            type = type,
            isUploaded = false,
            contents = contents,
            associatedUris = listOf(associatedUri),
        )
        val address = requireNotNull(userAddressRepository.getAddress(userId, addressId))
        secretsRepository.saveSecret(secret, address)
    }
}
