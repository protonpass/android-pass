package me.proton.core.pass.data.remote

import me.proton.core.domain.entity.UserId
import me.proton.core.network.data.ApiProvider
import me.proton.core.pass.common.api.Result
import me.proton.core.pass.common.api.toResult
import me.proton.core.pass.data.api.PasswordManagerApi
import me.proton.core.pass.data.responses.ItemKeyData
import me.proton.core.pass.data.responses.VaultKeyData
import me.proton.core.pass.domain.ShareId
import javax.inject.Inject

class RemoteVaultItemKeyDataSourceImpl @Inject constructor(
    private val api: ApiProvider
) : BaseRemoteDataSourceImpl(), RemoteVaultItemKeyDataSource {
    override suspend fun getKeys(
        userId: UserId,
        shareId: ShareId
    ): Result<VaultItemKeyResponseList> =
        api.get<PasswordManagerApi>(userId)
            .invoke {
                var page = 0
                val vaultKeys = mutableListOf<VaultKeyData>()
                val itemKeys = mutableListOf<ItemKeyData>()

                while (true) {
                    val pageKeys = getVaultKeys(shareId.id, page, PAGE_SIZE)
                    vaultKeys.addAll(pageKeys.keys.vaultKeys)
                    itemKeys.addAll(pageKeys.keys.itemKeys)

                    if (pageKeys.keys.itemKeys.size < PAGE_SIZE) {
                        break
                    } else {
                        page++
                    }
                }

                VaultItemKeyResponseList(vaultKeys, itemKeys)
            }
            .toResult()
}
