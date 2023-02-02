package proton.android.pass.data.impl.fakes

import proton.android.pass.data.api.repositories.VaultKeyRepository
import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.common.api.LoadingResult
import proton.pass.domain.ShareId
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.SigningKey
import proton.pass.domain.key.VaultKey

class TestVaultKeyRepository : VaultKeyRepository {

    private var latestVaultItemKey: LoadingResult<Pair<VaultKey, ItemKey>> = LoadingResult.Loading
    private var vaultKeys: LoadingResult<List<VaultKey>> = LoadingResult.Loading
    private var vaultKeyById: LoadingResult<VaultKey> = LoadingResult.Loading
    private var latestItemKey: LoadingResult<ItemKey> = LoadingResult.Loading
    private var latestVaultKey: LoadingResult<VaultKey> = LoadingResult.Loading

    fun setLatestVaultItemKey(value: LoadingResult<Pair<VaultKey, ItemKey>>) {
        latestVaultItemKey = value
    }

    fun setVaultKeys(value: LoadingResult<List<VaultKey>>) {
        vaultKeys = value
    }

    fun setVaultKeyById(value: LoadingResult<VaultKey>) {
        vaultKeyById = value
    }

    fun setLatestItemKey(value: LoadingResult<ItemKey>) {
        latestItemKey = value
    }

    fun setLatestVaultKey(value: LoadingResult<VaultKey>) {
        latestVaultKey = value
    }

    override suspend fun getVaultKeys(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean,
        shouldStoreLocally: Boolean
    ): LoadingResult<List<VaultKey>> = vaultKeys

    override suspend fun getVaultKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        keyId: String
    ): LoadingResult<VaultKey> = vaultKeyById

    override suspend fun getItemKeyById(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        keyId: String
    ): LoadingResult<ItemKey> = latestItemKey

    override suspend fun getLatestVaultKey(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean
    ): LoadingResult<VaultKey> = latestVaultKey

    override suspend fun getLatestVaultItemKey(
        userAddress: UserAddress,
        shareId: ShareId,
        signingKey: SigningKey,
        forceRefresh: Boolean
    ): LoadingResult<Pair<VaultKey, ItemKey>> = latestVaultItemKey
}
