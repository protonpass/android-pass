package proton.android.pass.crypto.api.usecases

import me.proton.core.user.domain.entity.UserAddress
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.VaultKey
import proton_pass_vault_v1.VaultV1

data class EncryptedCreateVault(
    val addressId: String,
    val content: String,
    val contentFormatVersion: Int,
    val contentEncryptedAddressSignature: String,
    val contentEncryptedVaultSignature: String,
    val vaultKey: String,
    val vaultKeyPassphrase: String,
    val vaultKeySignature: String,
    val keyPacket: String,
    val keyPacketSignature: String,
    val signingKey: String,
    val signingKeyPassphrase: String,
    val signingKeyPassphraseKeyPacket: String,
    val acceptanceSignature: String,
    val itemKey: String,
    val itemKeyPassphrase: String,
    val itemKeyPassphraseKeyPacket: String,
    val itemKeySignature: String
)

data class VaultKeyList(
    val vaultKeyList: List<VaultKey>,
    val itemKeyList: List<ItemKey>
)

interface CreateVault {
    fun createVaultRequest(
        vaultMetadata: VaultV1.Vault,
        userAddress: UserAddress
    ): Pair<EncryptedCreateVault, VaultKeyList>
}
