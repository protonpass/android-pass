package me.proton.core.pass.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.domain.entity.UserId
import me.proton.core.key.domain.decryptText
import me.proton.core.key.domain.encryptText
import me.proton.core.key.domain.useKeys
import me.proton.core.pass.data.db.datasources.SecretsDatabaseDataSource
import me.proton.core.pass.data.db.entities.SecretEntity
import me.proton.core.pass.data.extensions.toEntity
import me.proton.core.pass.data.extensions.toSecret
import me.proton.core.pass.domain.entity.commonsecret.Secret
import me.proton.core.pass.domain.repositories.SecretsRepository
import me.proton.core.user.domain.UserAddressManager
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

class SecretsRepositoryImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val dataSource: SecretsDatabaseDataSource,
    private val addressManager: UserAddressManager
) : SecretsRepository {

    override suspend fun saveSecret(secret: Secret, userAddress: UserAddress) {
        val encryptedSecretEntity = encrypt(secret, userAddress)
        dataSource.saveSecret(encryptedSecretEntity)
    }

    override fun observeSecrets(userId: UserId): Flow<List<Secret>> =
        dataSource.getAllSecretsForUser(userId.id).map { entities ->
            entities.map { decrypt(it, null) }
        }

    override fun observeSecrets(userAddress: UserAddress): Flow<List<Secret>> =
        dataSource.getAllSecretsForAddress(userAddress.addressId.id).map { entities ->
            entities.map { decrypt(it, userAddress) }
        }

    override suspend fun searchWithName(userAddress: UserAddress?, name: String): List<Secret> =
        dataSource.searchWithName(userAddress?.addressId?.id, name)
            .map { decrypt(it, userAddress) }

    override suspend fun searchWithUri(userAddress: UserAddress?, uri: String): List<Secret> =
        dataSource.searchWithUri(userAddress?.addressId?.id, uri)
            .map { decrypt(it, userAddress) }

    override suspend fun delete(secretId: String): Boolean = dataSource.delete(secretId)

    private fun encrypt(secret: Secret, address: UserAddress): SecretEntity {
        return address.useKeys(cryptoContext) {
            val secretEntity = secret.toEntity()
            val encryptedContents = encryptText(secretEntity.contents)
            secretEntity.copy(contents = encryptedContents)
        }
    }

    private suspend fun getAssociatedUserAddress(
        secretEntity: SecretEntity
    ): UserAddress? =
        addressManager.getAddress(UserId(secretEntity.userId), AddressId(secretEntity.addressId))

    private suspend fun decrypt(secretEntity: SecretEntity, address: UserAddress?): Secret {
        val address = (address ?: getAssociatedUserAddress(secretEntity)) ?: throw Exception()
        return address.useKeys(cryptoContext) {
            val textContents = secretEntity.contents
            val decryptedContents = decryptText(textContents)
            secretEntity.copy(contents = decryptedContents).toSecret()
        }
    }
}
