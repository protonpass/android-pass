package proton.android.pass.data.fakes.repositories

import me.proton.core.user.domain.entity.UserAddress
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.data.api.repositories.VaultKeyRepository
import proton.pass.domain.ShareId
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.SigningKey
import proton.pass.domain.key.VaultKey

class TestVaultKeyRepository : VaultKeyRepository {

    private var vaultKeys: LoadingResult<List<VaultKey>> = LoadingResult.Loading
    private var vaultKeyById: LoadingResult<VaultKey> = LoadingResult.Loading
    private var itemKeyById: LoadingResult<ItemKey> = LoadingResult.Loading
    private var latestVaultKey: LoadingResult<VaultKey> = LoadingResult.Loading
    private var latestVaultKeyItemKey: LoadingResult<Pair<VaultKey, ItemKey>> = LoadingResult.Loading

    fun setVaultKeys(value: LoadingResult<List<VaultKey>>) {
        vaultKeys = value
    }

    fun setVaultKeyById(value: LoadingResult<VaultKey>) {
        vaultKeyById = value
    }

    fun setItemKeyById(value: LoadingResult<ItemKey>) {
        itemKeyById = value
    }

    fun setLatestVaultKey(value: LoadingResult<VaultKey>) {
        latestVaultKey = value
    }

    fun setLatestVaultKeyItemKey(value: LoadingResult<Pair<VaultKey, ItemKey>>) {
        latestVaultKeyItemKey = value
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
    ): LoadingResult<ItemKey> = itemKeyById

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
    ): LoadingResult<Pair<VaultKey, ItemKey>> = latestVaultKeyItemKey
}
