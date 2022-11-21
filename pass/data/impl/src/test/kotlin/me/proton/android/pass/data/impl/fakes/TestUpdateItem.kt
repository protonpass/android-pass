package me.proton.android.pass.data.impl.fakes

import me.proton.android.pass.data.impl.crypto.UpdateItem
import me.proton.android.pass.data.impl.requests.UpdateItemRequest
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.domain.KeyPacket
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey
import proton_pass_item_v1.ItemV1

class TestUpdateItem : UpdateItem {

    private var request: UpdateItemRequest? = null

    fun setRequest(value: UpdateItemRequest) {
        request = value
    }

    override fun createRequest(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        keyPacket: KeyPacket,
        userAddress: UserAddress,
        itemContent: ItemV1.Item,
        lastRevision: Long
    ): UpdateItemRequest = request ?: throw IllegalStateException("request is not set")
}
