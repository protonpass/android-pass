package proton.android.pass.data.impl.extensions

import proton.pass.domain.ItemContents
import proton.pass.domain.entity.PackageName
import proton_pass_item_v1.ItemV1

fun ItemContents.serializeToProto(packageName: PackageName? = null): ItemV1.Item {
    val builder = ItemV1.Item.newBuilder()
        .setMetadata(
            ItemV1.Metadata.newBuilder()
                .setName(title)
                .setNote(note)
                .build()
        )
    if (packageName != null) {
        builder.setPlatformSpecific(
            ItemV1.PlatformSpecific.newBuilder()
                .setAndroid(
                    ItemV1.AndroidSpecific.newBuilder()
                        .addAllowedApps(
                            ItemV1.AllowedAndroidApp.newBuilder()
                                .setPackageName(packageName.packageName)
                                .build()
                        )
                        .build()
                )
                .build()
        )
    }
    val contentBuilder = ItemV1.Content.newBuilder()
    val content = when (this) {
        is ItemContents.Login -> {
            contentBuilder.setLogin(
                ItemV1.ItemLogin.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .addAllUrls(urls)
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
