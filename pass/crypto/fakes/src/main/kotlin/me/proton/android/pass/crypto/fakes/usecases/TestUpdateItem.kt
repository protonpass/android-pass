package me.proton.android.pass.crypto.fakes.usecases

import me.proton.android.pass.crypto.api.usecases.EncryptedUpdateItemRequest
import me.proton.android.pass.crypto.api.usecases.UpdateItem
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.domain.KeyPacket
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey
import proton_pass_item_v1.ItemV1

class TestUpdateItem : UpdateItem {

    private var request: EncryptedUpdateItemRequest? = null

    fun setRequest(value: EncryptedUpdateItemRequest) {
        request = value
    }

    override fun createRequest(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        keyPacket: KeyPacket,
        userAddress: UserAddress,
        itemContent: ItemV1.Item,
        lastRevision: Long
    ): EncryptedUpdateItemRequest = request ?: throw IllegalStateException("request is not set")
}
