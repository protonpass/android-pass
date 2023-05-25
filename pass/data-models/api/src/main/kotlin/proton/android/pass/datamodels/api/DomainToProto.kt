package proton.android.pass.datamodels.api

import proton.pass.domain.CustomFieldContent
import proton.pass.domain.ItemContents
import proton_pass_item_v1.ItemV1
import proton_pass_item_v1.extraField
import proton_pass_item_v1.extraHiddenField
import proton_pass_item_v1.extraTextField
import proton_pass_item_v1.extraTotp
import java.util.UUID

@Suppress("LongMethod")
fun ItemContents.serializeToProto(itemUuid: String? = null): ItemV1.Item {
    val uuid = itemUuid ?: UUID.randomUUID().toString()
    val builder = ItemV1.Item.newBuilder()
        .setMetadata(
            ItemV1.Metadata.newBuilder()
                .setName(title)
                .setNote(note)
                .setItemUuid(uuid)
                .build()
        )
    val contentBuilder = ItemV1.Content.newBuilder()
    val content = when (this) {
        is ItemContents.Login -> {
            if (packageInfoSet.isNotEmpty()) {
                val packageNameList = packageInfoSet.map {
                    ItemV1.AllowedAndroidApp.newBuilder()
                        .setPackageName(it.packageName.value)
                        .setAppName(it.appName.value)
                        .build()
                }
                builder.platformSpecific = ItemV1.PlatformSpecific.newBuilder()
                    .setAndroid(
                        ItemV1.AndroidSpecific.newBuilder()
                            .addAllAllowedApps(packageNameList)
                            .build()
                    )
                    .build()
            }

            for (customField in customFields) {
                when (customField) {
                    is CustomFieldContent.Text -> {
                        builder.addExtraFields(
                            extraField {
                                fieldName = customField.label
                                text = extraTextField {
                                    content = customField.value
                                }
                            }
                        )
                    }
                    is CustomFieldContent.Hidden -> {
                        builder.addExtraFields(
                            extraField {
                                fieldName = customField.label
                                hidden = extraHiddenField {
                                    content = customField.value
                                }
                            }
                        )
                    }
                    is CustomFieldContent.Totp -> {
                        builder.addExtraFields(
                            extraField {
                                fieldName = customField.label
                                totp = extraTotp {
                                    totpUri = customField.value
                                }
                            }
                        )
                    }
                }
            }

            contentBuilder.setLogin(
                ItemV1.ItemLogin.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .addAllUrls(urls)
                    .setTotpUri(primaryTotp)
                    .build()
            )
        }
        is ItemContents.Note -> contentBuilder.setNote(
            ItemV1.ItemNote.newBuilder().build()
        )
        is ItemContents.Alias -> contentBuilder.setAlias(
            ItemV1.ItemAlias.newBuilder().build()
        )
    }.build()

    return builder
        .setContent(content)
        .build()
}

