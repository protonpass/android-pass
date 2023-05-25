package proton.android.pass.data.impl.extensions

import kotlinx.datetime.Instant
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.CustomField
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import proton.pass.domain.entity.AppName
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName
import proton_pass_item_v1.ItemV1

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
            primaryTotp = context.encrypt(parsed.content.login.totpUri),
            customFields = parsed.extraFieldsList.map { field ->
                field.toDomain(context)
            }
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

fun ItemV1.ExtraField.toDomain(context: EncryptionContext): CustomField {
    return when (this.contentCase) {
        ItemV1.ExtraField.ContentCase.TEXT -> CustomField.Text(
            label = this.fieldName,
            value = this.text.content
        )
        ItemV1.ExtraField.ContentCase.HIDDEN -> CustomField.Hidden(
            label = this.fieldName,
            value = context.encrypt(this.hidden.content)
        )
        ItemV1.ExtraField.ContentCase.TOTP -> CustomField.Totp(
            label = this.fieldName,
            value = context.encrypt(this.totp.totpUri)
        )
        else -> {
            PassLogger.d("ItemType", "Unknown Custom field")
            CustomField.Unknown
        }
    }
}

fun ItemEntity.itemType(context: EncryptionContext): ItemType {
    val decrypted = context.decrypt(encryptedContent)
    val parsed = ItemV1.Item.parseFrom(decrypted)
    return ItemType.fromParsed(context, parsed, aliasEmail)
}

fun ItemEntity.allowedApps(context: EncryptionContext): Set<PackageInfo> {
    val decrypted = context.decrypt(encryptedContent)
    val parsed = ItemV1.Item.parseFrom(decrypted)
    return parsed.platformSpecific.android.allowedAppsList.map {
        PackageInfo(PackageName(it.packageName), AppName(it.appName))
    }.toSet()
}

fun ItemEntity.toDomain(context: EncryptionContext): Item {
    val decrypted = context.decrypt(encryptedContent)
    val parsed = ItemV1.Item.parseFrom(decrypted)

    return Item(
        id = ItemId(id),
        itemUuid = parsed.metadata.itemUuid,
        revision = revision,
        shareId = ShareId(shareId),
        itemType = ItemType.fromParsed(context, parsed, aliasEmail),
        title = encryptedTitle,
        note = encryptedNote,
        content = encryptedContent,
        state = state,
        packageInfoSet = allowedApps(context),
        modificationTime = Instant.fromEpochSeconds(modifyTime),
        createTime = Instant.fromEpochSeconds(createTime),
        lastAutofillTime = lastUsedTime.toOption().map(Instant::fromEpochSeconds)
    )
}
