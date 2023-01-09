package me.proton.android.pass.crypto.api.usecases

import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.domain.KeyPacket
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey
import proton_pass_item_v1.ItemV1

data class EncryptedUpdateItemRequest(
    val rotationId: String,
    val lastRevision: Long,
    val contentFormatVersion: Int,
    val content: String,
    val userSignature: String,
    val itemKeySignature: String
)

interface UpdateItem {
    @Suppress("LongParameterList")
    fun createRequest(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        keyPacket: KeyPacket,
        userAddress: UserAddress,
        itemContent: ItemV1.Item,
        lastRevision: Long
    ): EncryptedUpdateItemRequest
}
