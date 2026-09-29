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

package proton.android.pass.crypto.impl.usecases

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.datamodels.api.fromParsed
import proton.android.pass.datamodels.api.metadataIconOrNull
import proton.android.pass.datamodels.api.serializeToProto
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemFlags
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.domain.WorkDetailsContent
import proton.android.pass.domain.toItemContents
import proton.android.pass.domain.withIcon
import proton_pass_item_v1.ItemV1

class ItemIconProtoMappingTest {

    @Test
    fun `serializes the icon into the metadata`() {
        val proto = note(icon = ICON).serializeToProto(encryptionContext = FakeEncryptionContext)

        assertThat(proto.metadata.hasIcon()).isTrue()
        assertThat(proto.metadata.icon).isEqualTo(ICON)
        assertThat(proto.metadataIconOrNull()).isEqualTo(ICON)
    }

    @Test
    fun `does not set the icon for items without icon`() {
        val proto = note(icon = null).serializeToProto(encryptionContext = FakeEncryptionContext)
        val parsed = ItemV1.Item.parseFrom(proto.toByteArray())

        assertThat(parsed.metadata.hasIcon()).isFalse()
        assertThat(parsed.metadataIconOrNull()).isNull()
        assertThat(parsed.metadata.name).isEqualTo(TITLE)
    }

    @Test
    fun `preserves the icon through serialization for every item type`() {
        allItemTypes(icon = ICON).forEach { contents ->
            val decoded = roundTrip(contents)

            assertThat(decoded.icon).isEqualTo(ICON)
            assertThat(decoded::class).isEqualTo(contents::class)
            assertThat(decoded.title).isEqualTo(TITLE)
        }
    }

    @Test
    fun `keeps the icon when an item is edited`() {
        allItemTypes(icon = ICON).forEach { contents ->
            val existing = ItemV1.Item.parseFrom(
                contents.serializeToProto(encryptionContext = FakeEncryptionContext).toByteArray()
            )
            val decoded = decode(existing)

            // Same flow as the update screens: contents are decoded from the existing item,
            // edited, and serialized on top of the existing proto.
            val edited = decoded.withTitle("edited")
            val updated = edited.serializeToProto(
                builder = existing.toBuilder(),
                encryptionContext = FakeEncryptionContext
            )

            assertThat(updated.metadata.name).isEqualTo("edited")
            assertThat(updated.metadataIconOrNull()).isEqualTo(ICON)
        }
    }

    @Test
    fun `removes the icon when the edited contents have no icon`() {
        val existing = note(icon = ICON).serializeToProto(encryptionContext = FakeEncryptionContext)

        val updated = note(icon = null).serializeToProto(
            builder = existing.toBuilder(),
            encryptionContext = FakeEncryptionContext
        )

        assertThat(updated.metadata.hasIcon()).isFalse()
    }

    @Test
    fun `replaces the icon when the edited contents have another icon`() {
        val otherIcon = "data:image/png;base64,QUJD"
        val existing = note(icon = ICON).serializeToProto(encryptionContext = FakeEncryptionContext)

        val updated = note(icon = otherIcon).serializeToProto(
            builder = existing.toBuilder(),
            encryptionContext = FakeEncryptionContext
        )

        assertThat(updated.metadataIconOrNull()).isEqualTo(otherIcon)
    }

    @Test
    fun `serializes items without icon to identical bytes`() {
        val withoutIcon = note(icon = null).serializeToProto(itemUuid = UUID, encryptionContext = FakeEncryptionContext)
        val withIconRemoved = note(icon = ICON).withIcon(null)
            .serializeToProto(itemUuid = UUID, encryptionContext = FakeEncryptionContext)

        assertThat(withIconRemoved.toByteArray()).isEqualTo(withoutIcon.toByteArray())
    }

    private fun roundTrip(contents: ItemContents): ItemContents = decode(
        ItemV1.Item.parseFrom(contents.serializeToProto(encryptionContext = FakeEncryptionContext).toByteArray())
    )

    private fun decode(parsed: ItemV1.Item): ItemContents = toItemContents(
        decrypt = FakeEncryptionContext::decrypt,
        itemType = ItemType.fromParsed(FakeEncryptionContext, parsed, aliasEmail = ALIAS_EMAIL),
        title = FakeEncryptionContext.encrypt(parsed.metadata.name),
        note = FakeEncryptionContext.encrypt(parsed.metadata.note),
        itemFlags = ItemFlags(0),
        icon = parsed.metadataIconOrNull()
    )

    private fun ItemContents.withTitle(title: String): ItemContents = when (this) {
        is ItemContents.Login -> copy(title = title)
        is ItemContents.Note -> copy(title = title)
        is ItemContents.Alias -> copy(title = title)
        is ItemContents.CreditCard -> copy(title = title)
        is ItemContents.Identity -> copy(title = title)
        is ItemContents.Custom -> copy(title = title)
        is ItemContents.WifiNetwork -> copy(title = title)
        is ItemContents.SSHKey -> copy(title = title)
        is ItemContents.Unknown -> copy(title = title)
    }

    private fun note(icon: String?) = ItemContents.Note(
        title = TITLE,
        note = "note",
        customFields = emptyList(),
        icon = icon
    )

    private fun allItemTypes(icon: String?): List<ItemContents> {
        val empty = HiddenState.Empty(FakeEncryptionContext.encrypt(""))
        return listOf(
            ItemContents.Login.create(password = empty, primaryTotp = empty).copy(title = TITLE, icon = icon),
            note(icon),
            ItemContents.Alias(
                title = TITLE,
                note = "",
                customFields = emptyList(),
                aliasEmail = ALIAS_EMAIL,
                icon = icon
            ),
            ItemContents.CreditCard.default(cvv = empty, pin = empty).copy(title = TITLE, icon = icon),
            ItemContents.Identity(
                title = TITLE,
                note = "",
                customFields = emptyList(),
                personalDetailsContent = PersonalDetailsContent.EMPTY,
                addressDetailsContent = AddressDetailsContent.EMPTY,
                contactDetailsContent = ContactDetailsContent.default(FakeEncryptionContext::encrypt),
                workDetailsContent = WorkDetailsContent.EMPTY,
                extraSectionContentList = emptyList(),
                icon = icon
            ),
            ItemContents.Custom(
                title = TITLE,
                note = "",
                customFields = emptyList(),
                sectionContentList = emptyList(),
                icon = icon
            ),
            ItemContents.WifiNetwork(
                title = TITLE,
                note = "",
                customFields = emptyList(),
                ssid = "ssid",
                password = empty,
                wifiSecurityType = WifiSecurityType.WPA2,
                sectionContentList = emptyList(),
                icon = icon
            ),
            ItemContents.SSHKey(
                title = TITLE,
                note = "",
                customFields = emptyList(),
                publicKey = "public",
                privateKey = empty,
                sectionContentList = emptyList(),
                icon = icon
            )
        )
    }

    private companion object {
        const val TITLE = "title"
        const val UUID = "item-uuid"
        const val ALIAS_EMAIL = "alias@example.com"
        const val ICON = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk" +
            "+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
    }
}
