package proton.android.pass.crypto.api.usecases

import me.proton.core.user.domain.entity.User
import me.proton.core.user.domain.entity.UserAddress
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.VaultKey
import proton_pass_vault_v1.VaultV1

data class EncryptedCreateVault(
    val addressId: String,
    val content: String,
    val contentFormatVersion: Int,
    val encryptedVaultKey: String,
)

data class VaultKeyList(
    val vaultKeyList: List<VaultKey>,
    val itemKeyList: List<ItemKey>
)

data class CreateVaultOutput(
    val request: EncryptedCreateVault,
    val shareKey: ByteArray
)

interface CreateVault {
    fun createVaultRequest(
        user: User,
        userAddress: UserAddress,
        vaultMetadata: VaultV1.Vault
    ): CreateVaultOutput
}
