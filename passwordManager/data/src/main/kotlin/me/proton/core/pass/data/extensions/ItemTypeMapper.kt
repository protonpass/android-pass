package me.proton.core.pass.data.extensions

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.pass.domain.ItemType
import proton_pass_item_v1.ItemV1

fun ItemType.Companion.fromParsed(cryptoContext: CryptoContext, parsed: ItemV1.Item): ItemType {
    return when (parsed.content.contentCase) {
        ItemV1.Content.ContentCase.LOGIN -> ItemType.Login(
            username = parsed.content.login.username,
            password = parsed.content.login.password.encrypt(cryptoContext.keyStoreCrypto),
            websites = parsed.content.login.urlsList,
        )
        ItemV1.Content.ContentCase.NOTE -> ItemType.Note(parsed.metadata.note)
        ItemV1.Content.ContentCase.ALIAS -> ItemType.Alias
        else -> throw Exception("Unknown ItemType")
    }
}
