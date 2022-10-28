package me.proton.pass.domain

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString

@JvmInline
value class ItemId(val id: String)

data class Item(
    val id: ItemId,
    val revision: Long,
    val shareId: ShareId,
    val itemType: ItemType,
    val title: EncryptedString,
    val note: EncryptedString,
    val content: EncryptedByteArray,
    val allowedPackageNames: List<String>
)
