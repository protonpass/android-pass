package me.proton.core.pass.data.extensions

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.pass.domain.ItemType
import proton_key_item_v1.ItemV1

fun ItemType.Companion.fromParsed(cryptoContext: CryptoContext, parsed: ItemV1.Item): ItemType {
    return when (parsed.contentCase) {
        ItemV1.Item.ContentCase.LOGIN -> ItemType.Login(
            username = parsed.login.username,
            password = parsed.login.password.encrypt(cryptoContext.keyStoreCrypto)
        )

        ItemV1.Item.ContentCase.NOTE -> ItemType.Note(
            text = parsed.note.content
        )
        else -> throw Exception("Unknown ItemType")
    }
}
