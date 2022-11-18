package me.proton.android.pass.data.impl.extensions

import me.proton.android.pass.data.impl.db.entities.ItemEntity
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.pass.domain.ItemType
import proton_pass_item_v1.ItemV1

@Suppress("TooGenericExceptionThrown")
fun ItemType.Companion.fromParsed(
    cryptoContext: CryptoContext,
    parsed: ItemV1.Item,
    aliasEmail: String? = null
): ItemType {
    return when (parsed.content.contentCase) {
        ItemV1.Content.ContentCase.LOGIN -> ItemType.Login(
            username = parsed.content.login.username,
            password = parsed.content.login.password.encrypt(cryptoContext.keyStoreCrypto),
            websites = parsed.content.login.urlsList
        )
        ItemV1.Content.ContentCase.NOTE -> ItemType.Note(parsed.metadata.note)
        ItemV1.Content.ContentCase.ALIAS -> {
            requireNotNull(aliasEmail)
            ItemType.Alias(aliasEmail = aliasEmail)
        }
        else -> throw Exception("Unknown ItemType")
    }
}

fun ItemEntity.itemType(cryptoContext: CryptoContext): ItemType {
    val decrypted = encryptedContent.decrypt(cryptoContext.keyStoreCrypto)
    val parsed = ItemV1.Item.parseFrom(decrypted.array)
    return ItemType.fromParsed(cryptoContext, parsed, aliasEmail)
}

fun ItemEntity.allowedApps(cryptoContext: CryptoContext): List<String> {
    val decrypted = encryptedContent.decrypt(cryptoContext.keyStoreCrypto)
    val parsed = ItemV1.Item.parseFrom(decrypted.array)
    return parsed.platformSpecific.android.allowedAppsList.map { it.packageName }
}
