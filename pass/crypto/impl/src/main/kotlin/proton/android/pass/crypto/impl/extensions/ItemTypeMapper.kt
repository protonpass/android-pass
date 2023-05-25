package proton.android.pass.crypto.impl.extensions

import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ItemType
import proton.pass.domain.entity.AppName
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName
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
            packageInfoSet = parsed.platformSpecific.android.allowedAppsList.map {
                PackageInfo(PackageName(it.packageName), AppName(it.appName))
            }.toSet(),
            primaryTotp = context.encrypt(parsed.content.login.totpUri)
        )
        ItemV1.Content.ContentCase.NOTE -> ItemType.Note(parsed.metadata.note)
        ItemV1.Content.ContentCase.ALIAS -> {
            requireNotNull(aliasEmail)
            ItemType.Alias(aliasEmail = aliasEmail)
        }
        else -> {
            PassLogger.d("ItemType", "Unknown item type")
            ItemType.Unknown
        }
    }
}
