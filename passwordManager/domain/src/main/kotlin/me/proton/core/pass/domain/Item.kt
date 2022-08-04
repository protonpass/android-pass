package me.proton.core.pass.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString

data class ItemId(val id: String)

data class Item(
    val id: ItemId,
    val shareId: ShareId,
    val itemType: ItemType,
    val title: EncryptedString,
    val note: EncryptedString,
    val content: EncryptedByteArray,
)
