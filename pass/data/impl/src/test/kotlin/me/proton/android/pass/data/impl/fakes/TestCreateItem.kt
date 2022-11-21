package me.proton.android.pass.data.impl.fakes

import me.proton.android.pass.data.impl.crypto.CreateItem
import me.proton.android.pass.data.impl.requests.CreateItemRequest
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.entity.PackageName
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey
import me.proton.pass.test.crypto.TestKeyStoreCrypto

class TestCreateItem : CreateItem {

    private var request: CreateItemRequest? = null

    fun setRequest(value: CreateItemRequest) {
        request = value
    }

    override fun create(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        userAddress: UserAddress,
        itemContents: ItemContents,
        packageName: PackageName?
    ): CreateItemRequest = request ?: throw IllegalStateException("request is not set")

    companion object {
        fun createRequest() = CreateItemRequest(
            rotationId = "testRotationId",
            labels = emptyList(),
            vaultKeyPacket = "vaultKeyPacket",
            vaultKeyPacketSignature = "vaultKeyPacketSignature",
            contentFormatVersion = 1,
            content = TestKeyStoreCrypto.encrypt("content"),
            userSignature = "userSignature",
            itemKeySignature = "itemKeySignature"
        )
    }
}
