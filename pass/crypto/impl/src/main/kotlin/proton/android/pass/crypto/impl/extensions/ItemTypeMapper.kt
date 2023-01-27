package proton.android.pass.crypto.impl.extensions

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.pass.domain.ItemType
import proton_pass_item_v1.ItemV1


@Suppress("TooGenericExceptionThrown")
fun ItemType.Companion.fromParsed(
    context: EncryptionContext,
    parsed: ItemV1.Item,
    aliasEmail: String? = null
): ItemType {
    return when (parsed.content.contentCase) {
        ItemV1.Content.ContentCase.LOGIN -> ItemType.Login(
            username = parsed.content.login.username,
            password = context.encrypt(parsed.content.login.password),
            websites = parsed.content.login.urlsList,
            primaryTotp = context.encrypt(parsed.content.login.totpUri)
        )
        ItemV1.Content.ContentCase.NOTE -> ItemType.Note(parsed.metadata.note)
        ItemV1.Content.ContentCase.ALIAS -> {
            requireNotNull(aliasEmail)
            ItemType.Alias(aliasEmail = aliasEmail)
        }
        else -> throw Exception("Unknown ItemType")
    }
}
