package proton.android.pass.commonui.api

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.datamodels.api.toContent
import proton.pass.domain.HiddenState
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemType

fun Item.toUiModel(context: EncryptionContext): ItemUiModel =
    ItemUiModel(
        id = id,
        shareId = shareId,
        contents = toItemContents(context),
        state = state,
        createTime = createTime,
        modificationTime = modificationTime,
        lastAutofillTime = lastAutofillTime.value(),
    )

fun Item.itemName(context: EncryptionContext): String =
    context.decrypt(title)

fun Item.loginUsername(): Option<String> = when (val type = itemType) {
    is ItemType.Login -> type.username.toOption()
    else -> None
}

fun Item.toItemContents(encryptionContext: EncryptionContext): ItemContents =
    when (val type = itemType) {
        is ItemType.Alias -> ItemContents.Alias(
            title = encryptionContext.decrypt(title),
            note = encryptionContext.decrypt(note),
            aliasEmail = type.aliasEmail
        )

        is ItemType.Login -> ItemContents.Login(
            title = encryptionContext.decrypt(title),
            note = encryptionContext.decrypt(note),
            username = type.username,
            password = HiddenState.Concealed(type.password),
            urls = type.websites,
            packageInfoSet = type.packageInfoSet,
            primaryTotp = HiddenState.Concealed(type.primaryTotp),
            customFields = type.customFields.mapNotNull { it.toContent(encryptionContext, true) }
        )

        is ItemType.Note -> ItemContents.Note(
            title = encryptionContext.decrypt(title),
            note = encryptionContext.decrypt(note)
        )

        ItemType.Password,
        ItemType.Unknown -> ItemContents.Unknown(
            title = encryptionContext.decrypt(title),
            note = encryptionContext.decrypt(note)
        )
    }
