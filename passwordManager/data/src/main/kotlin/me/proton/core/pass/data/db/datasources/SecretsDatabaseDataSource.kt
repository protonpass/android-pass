package me.proton.core.pass.data.db.datasources

import kotlinx.coroutines.flow.Flow
import me.proton.core.pass.data.db.PassDatabase
import me.proton.core.pass.data.db.entities.SecretEntity

class SecretsDatabaseDataSource(
    private val database: PassDatabase
) {
    suspend fun saveSecret(secretEntity: SecretEntity) =
        database.secretsDao().insertOrUpdate(secretEntity)

    suspend fun searchWithName(addressId: String?, name: String): List<SecretEntity> =
        if (addressId != null)
            database.secretsDao().searchWithName(addressId, name)
        else database.secretsDao().searchWithName(name)

    suspend fun searchWithUri(addressId: String?, uri: String): List<SecretEntity> =
        if (addressId != null)
            database.secretsDao().searchWithUri(addressId, uri)
        else database.secretsDao().searchWithUri(uri)

    fun getAllSecretsForUser(userId: String): Flow<List<SecretEntity>> =
        database.secretsDao().observeAllForUser(userId)

    fun getAllSecretsForAddress(addressId: String): Flow<List<SecretEntity>> =
        database.secretsDao().observeAllForAddress(addressId)

    suspend fun delete(secretId: String): Boolean = database.secretsDao().delete(secretId) > 0
}
