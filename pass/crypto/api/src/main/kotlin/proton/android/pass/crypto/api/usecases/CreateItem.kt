package proton.android.pass.crypto.api.usecases

import me.proton.core.user.domain.entity.UserAddress
import proton.pass.domain.ItemContents
import proton.pass.domain.key.ItemKey
import proton.pass.domain.key.VaultKey

data class EncryptedCreateItem(
    val rotationId: String,
    val labels: List<String>,
    val vaultKeyPacket: String,
    val vaultKeyPacketSignature: String,
    val contentFormatVersion: Int,
    val content: String,
    val userSignature: String,
    val itemKeySignature: String
)

interface CreateItem {
    fun create(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        userAddress: UserAddress,
        itemContents: ItemContents
    ): EncryptedCreateItem
}
