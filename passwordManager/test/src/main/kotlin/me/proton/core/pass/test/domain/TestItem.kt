package me.proton.core.pass.test.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemId
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.domain.ShareId

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
}
