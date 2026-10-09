/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.commonrust.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.commonrust.ProtonPassSerialization
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.attachments.AttachmentContent
import proton.android.pass.types.AliasItem
import proton.android.pass.types.ItemContent
import proton.android.pass.types.ItemData
import proton.android.pass.types.NoteItem

internal class ItemContentSerializerImplTest {

    private val serializer = ItemContentSerializerImpl()
    private val serialization = ProtonPassSerialization()
    private val icon = byteArrayOf(1, 2, 3, 4)
    private val unknownTopLevelField = byteArrayOf(0xBA.toByte(), 0x3E, 0x03, 0x0A, 0x0B, 0x0C)
    private val metadataWithName = byteArrayOf(0x0A, 0x03, 0x0A, 0x01, 0x54)
    private val contentWithUnknownField = byteArrayOf(0x12, 0x03, 0x9A.toByte(), 0x06, 0x00)

    @Test
    fun performItemUpdatePreservesUnknownTopLevelFieldAndCustomIcon() {
        val original = serialization.itemDataSerialize(libraryNote()) + unknownTopLevelField

        val updated = serializer.performItemUpdate(original, domainNote(title = "New title"), FakeEncryptionContext)

        assertThat(indexOf(updated, unknownTopLevelField)).isAtLeast(0)
        val reparsed = serialization.itemDataDeserialize(updated)
        assertThat(reparsed.title).isEqualTo("New title")
        assertThat(reparsed.itemUuid).isEqualTo("uuid")
        assertThat(reparsed.customIcon).isEqualTo(icon)
    }

    @Test
    fun serializeThenDeserializeKeepsTitleAndNote() {
        val bytes = serializer.serializeItem(domainNote(title = "Title"), FakeEncryptionContext, "uuid-1")

        val parsed = serializer.deserializeItem(bytes, FakeEncryptionContext)

        assertThat(parsed.itemUuid).isEqualTo("uuid-1")
        assertThat(parsed.title).isEqualTo("Title")
        assertThat(parsed.note).isEqualTo("note")
        assertThat(parsed.itemType).isInstanceOf(ItemType.Note::class.java)
    }

    @Test
    fun deserializeItemsReportsUnknownForEntryThatCannotBeMappedWithoutFailingTheBatch() {
        val good = serializer.serializeItem(domainNote(title = "Good"), FakeEncryptionContext, "uuid-good")
        val alias = serialization.itemDataSerialize(
            ItemData(
                title = "Alias",
                note = "",
                itemUuid = "uuid-alias",
                content = ItemContent.Alias(AliasItem()),
                extraFields = emptyList(),
                platformSpecific = null,
                customIcon = null
            )
        )

        val parsed = serializer.deserializeItems(
            bytes = listOf(good, alias, good),
            encryptionContext = FakeEncryptionContext,
            aliasEmails = listOf(null, null, null)
        )

        assertThat(parsed).hasSize(3)
        assertThat(parsed[0].itemType).isInstanceOf(ItemType.Note::class.java)
        assertThat(parsed[1].itemType).isEqualTo(ItemType.Unknown)
        assertThat(parsed[2].itemType).isInstanceOf(ItemType.Note::class.java)
    }

    @Test
    fun deserializeItemReportsUnknownForAliasWithoutAliasEmail() {
        val alias = serialization.itemDataSerialize(
            ItemData(
                title = "Alias",
                note = "",
                itemUuid = "uuid-alias",
                content = ItemContent.Alias(AliasItem()),
                extraFields = emptyList(),
                platformSpecific = null,
                customIcon = null
            )
        )

        val parsed = serializer.deserializeItem(alias, FakeEncryptionContext, aliasEmail = null)

        assertThat(parsed.itemType).isEqualTo(ItemType.Unknown)
    }

    @Test
    fun deserializeItemReportsUnknownForUnknownContent() {
        val bytes = metadataWithName + contentWithUnknownField

        val parsed = serializer.deserializeItem(bytes, FakeEncryptionContext)

        assertThat(parsed.itemType).isEqualTo(ItemType.Unknown)
    }

    @Test
    fun deserializeItemsReportsUnknownForUnknownContentWithoutFailingTheBatch() {
        val good = serializer.serializeItem(domainNote(title = "Good"), FakeEncryptionContext, "uuid-good")
        val unknown = metadataWithName + contentWithUnknownField

        val parsed = serializer.deserializeItems(
            bytes = listOf(good, unknown, good),
            encryptionContext = FakeEncryptionContext,
            aliasEmails = listOf(null, null, null)
        )

        assertThat(parsed).hasSize(3)
        assertThat(parsed[0].itemType).isInstanceOf(ItemType.Note::class.java)
        assertThat(parsed[1].itemType).isEqualTo(ItemType.Unknown)
        assertThat(parsed[2].itemType).isInstanceOf(ItemType.Note::class.java)
    }

    @Test
    fun updateAttachmentNameChangesTheNameAndKeepsTheMimeType() {
        val original = serializer.serializeAttachmentContent(
            AttachmentContent(name = "old.png", mimeType = "image/png")
        )

        val updated = serializer.updateAttachmentName(original, "new.png")

        val parsed = serializer.deserializeAttachmentContent(updated)
        assertThat(parsed.name).isEqualTo("new.png")
        assertThat(parsed.mimeType).isEqualTo("image/png")
    }

    private fun libraryNote() = ItemData(
        title = "Old title",
        note = "",
        itemUuid = "uuid",
        content = ItemContent.Note(NoteItem()),
        extraFields = emptyList(),
        platformSpecific = null,
        customIcon = icon
    )

    private fun domainNote(title: String) = ItemContents.Note(title = title, note = "note", customFields = emptyList())

    private fun indexOf(source: ByteArray, target: ByteArray): Int =
        (0..source.size - target.size).firstOrNull { start ->
            target.indices.all { source[start + it] == target[it] }
        } ?: -1
}
