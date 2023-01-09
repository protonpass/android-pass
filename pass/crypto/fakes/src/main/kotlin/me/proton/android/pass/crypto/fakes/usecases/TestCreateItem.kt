package me.proton.android.pass.crypto.fakes.usecases

import me.proton.android.pass.crypto.api.usecases.CreateItem
import me.proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import me.proton.core.user.domain.entity.UserAddress
import me.proton.pass.domain.ItemContents
import me.proton.pass.domain.entity.PackageName
import me.proton.pass.domain.key.ItemKey
import me.proton.pass.domain.key.VaultKey
import me.proton.pass.test.crypto.TestKeyStoreCrypto

class TestCreateItem : CreateItem {

    private var request: EncryptedCreateItem? = null

    fun setRequest(value: EncryptedCreateItem) {
        request = value
    }

    override fun create(
        vaultKey: VaultKey,
        itemKey: ItemKey,
        userAddress: UserAddress,
        itemContents: ItemContents,
        packageName: PackageName?
    ): EncryptedCreateItem = request ?: throw IllegalStateException("request is not set")

    companion object {
        fun createRequest() = EncryptedCreateItem(
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
