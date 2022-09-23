package me.proton.core.pass.test.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId
import me.proton.core.pass.test.TestUtils.randomString
import me.proton.core.pass.test.crypto.TestKeyStoreCrypto
import kotlin.random.Random

object TestItem {

    fun create(): Item = Item(
        id = ItemId(id = "item-id"),
        revision = 0,
        shareId = ShareId(id = "share-id"),
        itemType = ItemType.Password,
        title = "item-title",
        note = "item-note",
        content = EncryptedByteArray(byteArrayOf())
    )

    fun random(itemType: ItemType? = null, title: String? = null, note: String? = null): Item {
        val itemTypeParam = itemType ?: ItemType.Login(
            randomString(),
            randomString().encrypt(TestKeyStoreCrypto),
            emptyList()
        )
        val titleParam = title ?: randomString()
        val noteParam = note ?: randomString()
        return Item(
            id = ItemId(randomString()),
            revision = Random.nextLong(),
            shareId = ShareId(randomString()),
            itemType = itemTypeParam,
            title = TestKeyStoreCrypto.encrypt(titleParam),
            note = TestKeyStoreCrypto.encrypt(noteParam),
            content = EncryptedByteArray(byteArrayOf())
        )
    }
}
