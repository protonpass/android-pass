package proton.android.pass.test.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.key.domain.entity.key.PrivateAddressKey
import me.proton.core.key.domain.entity.key.PrivateKey
import proton.android.pass.domain.Group
import proton.android.pass.domain.GroupAddress
import proton.android.pass.domain.GroupId

object GroupTestFactory {
    fun create(
        id: GroupId = GroupId("group-id"),
        email: String = "group@test.com",
        includeKeys: Boolean = true
    ): Group = Group(
        id = id,
        name = "group",
        address = GroupAddress(
            id = "address-id",
            email = email,
            keys = if (includeKeys) listOf(defaultPrivateAddressKey()) else emptyList()
        ),
        permissions = 0,
        createTime = 0,
        flags = 0,
        groupVisibility = 0,
        memberVisibility = 0,
        description = null
    )

    private fun defaultPrivateAddressKey(): PrivateAddressKey = PrivateAddressKey(
        addressId = "address-id",
        privateKey = PrivateKey(
            key = "priv-key",
            isPrimary = true,
            isActive = true,
            canEncrypt = true,
            canVerify = true,
            passphrase = EncryptedByteArray(byteArrayOf())
        ),
        token = "token",
        signature = null
    )
}
