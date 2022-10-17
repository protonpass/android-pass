package me.proton.core.pass.domain.repositories

import me.proton.core.pass.common.api.Result
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.domain.key.ItemKey
import me.proton.core.pass.domain.key.SigningKey
import me.proton.core.pass.domain.key.VaultKey
import me.proton.core.user.domain.entity.UserAddress

data class VaultItemKeyList(
    val vaultKeyList: List<VaultKey>,
    val itemKeyList: List<ItemKey>
)

interface VaultKeyRepository {
    suspend fun getVaultKeys(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean = false,
        shouldStoreLocally: Boolean = true
    ): Result<List<VaultKey>>

    suspend fun getVaultKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        keyId: String
    ): Result<VaultKey>

    suspend fun getItemKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        keyId: String
    ): Result<ItemKey>

    suspend fun getLatestVaultKey(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean = false
    ): Result<VaultKey>

    suspend fun getLatestVaultItemKey(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean = false
    ): Result<Pair<VaultKey, ItemKey>>
}
