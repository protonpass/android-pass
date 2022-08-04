package me.proton.core.pass.data.extensions

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.data.db.entities.ItemEntity
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemType
import proton_pass_item_v1.ItemV1

fun Item.name(cryptoContext: CryptoContext): String =
    title.decrypt(cryptoContext.keyStoreCrypto)

fun ItemEntity.itemType(cryptoContext: CryptoContext): ItemType {
    val decrypted = encryptedContent.decrypt(cryptoContext.keyStoreCrypto)
    val parsed = ItemV1.Item.parseFrom(decrypted.array)
    return ItemType.fromParsed(cryptoContext, parsed)
}
