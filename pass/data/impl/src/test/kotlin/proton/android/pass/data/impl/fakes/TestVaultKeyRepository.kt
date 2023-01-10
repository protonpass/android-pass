package proton.android.pass.data.impl.fakes

import proton.android.pass.data.api.repositories.VaultKeyRepository
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.common.api.Result
import proton.pass.domain.ShareId
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.SigningKey
import proton.pass.domain.key.VaultKey

class TestVaultKeyRepository : VaultKeyRepository {

    private var latestVaultItemKey: Result<Pair<VaultKey, ItemKey>> = Result.Loading
    private var vaultKeys: Result<List<VaultKey>> = Result.Loading
    private var vaultKeyById: Result<VaultKey> = Result.Loading
    private var latestItemKey: Result<ItemKey> = Result.Loading
    private var latestVaultKey: Result<VaultKey> = Result.Loading

    fun setLatestVaultItemKey(value: Result<Pair<VaultKey, ItemKey>>) {
        latestVaultItemKey = value
    }

    fun setVaultKeys(value: Result<List<VaultKey>>) {
        vaultKeys = value
    }

    fun setVaultKeyById(value: Result<VaultKey>) {
        vaultKeyById = value
    }

    fun setLatestItemKey(value: Result<ItemKey>) {
        latestItemKey = value
    }

    fun setLatestVaultKey(value: Result<VaultKey>) {
        latestVaultKey = value
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
    ): Result<ItemKey> = latestItemKey

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
    ): Result<Pair<VaultKey, ItemKey>> = latestVaultItemKey
}
