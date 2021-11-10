package me.proton.core.pass.domain.repositories

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import me.proton.core.pass.common_secret.Secret
import me.proton.core.user.domain.entity.UserAddress

interface SecretsRepository {
    suspend fun saveSecret(secret: Secret, userAddress: UserAddress)
    fun observeSecrets(userId: UserId): Flow<List<Secret>>
    fun observeSecrets(userAddress: UserAddress): Flow<List<Secret>>
    suspend fun searchWithName(userAddress: UserAddress?, name: String): List<Secret>
    suspend fun searchWithUri(userAddress: UserAddress?, uri: String): List<Secret>
    suspend fun delete(secretId: String): Boolean
}
