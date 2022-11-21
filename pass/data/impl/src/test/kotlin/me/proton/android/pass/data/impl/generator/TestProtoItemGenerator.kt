package me.proton.android.pass.data.impl.generator

import proton_pass_item_v1.ItemV1

object TestProtoItemGenerator {
    fun generate(
        name: String = "name",
        note: String = "note"
    ): ItemV1.Item =
        ItemV1.Item.newBuilder()
            .setMetadata(
                ItemV1.Metadata.newBuilder()
                    .setName(name)
                    .setNote(note)
                    .build()
            )
            .setContent(
                ItemV1.Content.newBuilder()
                    .setNote(ItemV1.ItemNote.newBuilder().build())
                    .build()
            )
            .build()


    fun generateByteArray() = generate().toByteArray()
}
