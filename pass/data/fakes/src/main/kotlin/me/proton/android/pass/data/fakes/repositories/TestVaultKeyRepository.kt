package me.proton.android.pass.data.fakes.repositories

import me.proton.android.pass.data.api.repositories.VaultKeyRepository
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.common.api.Result
import me.proton.pass.domain.ShareId
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.SigningKey
import me.proton.pass.domain.key.VaultKey

class TestVaultKeyRepository : VaultKeyRepository {

    private var vaultKeys: Result<List<VaultKey>> = Result.Loading
    private var vaultKeyById: Result<VaultKey> = Result.Loading
    private var itemKeyById: Result<ItemKey> = Result.Loading
    private var latestVaultKey: Result<VaultKey> = Result.Loading
    private var latestVaultKeyItemKey: Result<Pair<VaultKey, ItemKey>> = Result.Loading

    fun setVaultKeys(value: Result<List<VaultKey>>) {
        vaultKeys = value
    }

    fun setVaultKeyById(value: Result<VaultKey>) {
        vaultKeyById = value
    }

    fun setItemKeyById(value: Result<ItemKey>) {
        itemKeyById = value
    }

    fun setLatestVaultKey(value: Result<VaultKey>) {
        latestVaultKey = value
    }

    fun setLatestVaultKeyItemKey(value: Result<Pair<VaultKey, ItemKey>>) {
        latestVaultKeyItemKey = value
    }

    override suspend fun getVaultKeys(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean,
        shouldStoreLocally: Boolean
    ): Result<List<VaultKey>> = vaultKeys

    override suspend fun getVaultKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        keyId: String
    ): Result<VaultKey> = vaultKeyById

    override suspend fun getItemKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        keyId: String
    ): Result<ItemKey> = itemKeyById

    override suspend fun getLatestVaultKey(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean
    ): Result<VaultKey> = latestVaultKey

    override suspend fun getLatestVaultItemKey(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean
    ): Result<Pair<VaultKey, ItemKey>> = latestVaultKeyItemKey
}
