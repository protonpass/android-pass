package me.proton.core.pass.data.extensions

import me.proton.core.pass.domain.ItemContents
import proton_pass_item_v1.ItemV1

fun ItemContents.serializeToProto(): ItemV1.Item {
    val builder = ItemV1.Item.newBuilder()
        .setMetadata(
            ItemV1.Metadata.newBuilder()
                .setName(title)
                .setNote(note)
                .build()
        )
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
