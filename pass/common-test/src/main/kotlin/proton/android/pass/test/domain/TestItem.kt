/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.test.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.domain.entity.UserId
import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.common.api.None
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.datamodels.api.fromParsed
import proton.android.pass.datamodels.api.serializeToProto
import proton.android.pass.domain.AddressDetailsContent
import proton.android.pass.domain.ContactDetailsContent
import proton.android.pass.domain.CreditCardType
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemFlags
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.PersonalDetailsContent
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.WorkDetailsContent
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.test.TestUtils.randomString
import java.util.UUID
import kotlin.random.Random

object TestItem {

    fun create(
        itemType: ItemType = ItemType.Password,
        itemId: ItemId = ItemId(id = "item-id"),
        shareId: ShareId = ShareId(id = "share-id"),
        packageInfoSet: Set<PackageInfo> = emptySet(),
        keyStoreCrypto: KeyStoreCrypto = TestKeyStoreCrypto,
        title: String = "item-title"
    ): Item {
        val note = "item-note"
        val now = Clock.System.now()
        return Item(
            id = itemId,
            userId = UserId("user-id"),
            itemUuid = "uuid",
            revision = 0,
            shareId = shareId,
            itemType = itemType,
            title = title.encrypt(keyStoreCrypto),
            note = note.encrypt(keyStoreCrypto),
            content = EncryptedByteArray(byteArrayOf()),
            packageInfoSet = packageInfoSet,
            state = 0,
            modificationTime = now,
            createTime = now,
            lastAutofillTime = None,
            isPinned = false,
            pinTime = None,
            itemFlags = ItemFlags(0),
            shareCount = 0,
            shareType = ShareType.Vault
        )
    }

    fun create(
        shareId: ShareId = ShareId("share-123"),
        itemId: ItemId = ItemId("item-123"),
        aliasEmail: String? = null,
        flags: Int = 0,
        itemContents: ItemContents
    ): Item {
        val now = Clock.System.now()
        val asProto = itemContents.serializeToProto(
            itemUuid = "123",
            encryptionContext = TestEncryptionContext
        )
        return Item(
            id = itemId,
            userId = UserId("user-id"),
            itemUuid = "",
            revision = 1,
            shareId = shareId,
            itemType = ItemType.fromParsed(TestEncryptionContext, asProto, aliasEmail),
            title = TestKeyStoreCrypto.encrypt(itemContents.title),
            note = TestKeyStoreCrypto.encrypt(itemContents.note),
            content = TestKeyStoreCrypto.encrypt(PlainByteArray(asProto.toByteArray())),
            packageInfoSet = emptySet(),
            state = ItemState.Active.value,
            modificationTime = now,
            createTime = now,
            lastAutofillTime = None,
            isPinned = false,
            pinTime = None,
            itemFlags = ItemFlags(flags),
            shareCount = 0,
            shareType = ShareType.Vault
        )
    }

    fun createLogin(
        shareId: ShareId = ShareId("share-123"),
        itemId: ItemId = ItemId("item-123"),
        title: String = "login-item",
        email: String = "user@email.com",
        username: String = "username",
        password: String = "",
        primaryTotp: String = "",
        note: String = "note"
    ): Item = create(
        shareId = shareId,
        itemId = itemId,
        itemContents = ItemContents.Login(
            title = title,
            note = note,
            itemEmail = email,
            itemUsername = username,
            password = HiddenState.Concealed(TestEncryptionContext.encrypt(password)),
            urls = emptyList(),
            packageInfoSet = emptySet(),
            primaryTotp = HiddenState.Revealed(
                TestEncryptionContext.encrypt(primaryTotp),
                primaryTotp
            ),
            customFields = emptyList(),
            passkeys = emptyList()
        )
    )

    fun createAlias(
        shareId: ShareId = ShareId("share-123"),
        itemId: ItemId = ItemId("item-123"),
        title: String = "alias-item",
        alias: String = "some.alias@domain.test",
        note: String = "note",
        flags: Int = 0
    ): Item = create(
        shareId = shareId,
        itemId = itemId,
        aliasEmail = alias,
        flags = flags,
        itemContents = ItemContents.Alias(
            title = title,
            note = note,
            aliasEmail = alias,
            customFields = emptyList()
        )
    )

    fun createNote(
        shareId: ShareId = ShareId("share-123"),
        itemId: ItemId = ItemId("item-123"),
        title: String = "note-item",
        note: String = "note"
    ): Item = create(
        shareId = shareId,
        itemId = itemId,
        itemContents = ItemContents.Note(
            title = title,
            note = note,
            customFields = emptyList()
        )
    )

    fun createCreditCard(
        shareId: ShareId = ShareId("share-123"),
        itemId: ItemId = ItemId("item-123"),
        title: String = "note-item",
        note: String = "note",
        holder: String = "Card holder",
        number: String = "1234123412341234",
        pin: String = "0000",
        verificationNumber: String = "000",
        expirationDate: String = "2030-01"
    ): Item = create(
        shareId = shareId,
        itemId = itemId,
        itemContents = ItemContents.CreditCard(
            title = title,
            note = note,
            cardHolder = holder,
            type = CreditCardType.Other,
            number = number,
            cvv = if (verificationNumber.isBlank()) {
                HiddenState.Empty(TestEncryptionContext.encrypt(verificationNumber))
            } else {
                HiddenState.Concealed(TestEncryptionContext.encrypt(verificationNumber))
            },
            pin = if (pin.isBlank()) {
                HiddenState.Empty(TestEncryptionContext.encrypt(pin))
            } else {
                HiddenState.Concealed(TestEncryptionContext.encrypt(pin))
            },
            expirationDate = expirationDate,
            customFields = emptyList()
        )
    )

    fun createIdentity(
        shareId: ShareId = ShareId("share-123"),
        itemId: ItemId = ItemId("item-123"),
        fullName: String = "John Doe"
    ): Item = create(
        shareId = shareId,
        itemId = itemId,
        itemContents = ItemContents.Identity(
            title = "Identity",
            note = "note",
            personalDetailsContent = PersonalDetailsContent.EMPTY.copy(
                fullName = fullName,
                firstName = "First name",
                middleName = "Middle name",
                lastName = "Last name",
                phoneNumber = "1234567890"
            ),
            addressDetailsContent = AddressDetailsContent.EMPTY.copy(
                streetAddress = "123 Main St",
                zipOrPostalCode = "12345",
                city = "City",
                countryOrRegion = "Country",
                organization = "Organization"
            ),
            contactDetailsContent = ContactDetailsContent.default {
                TestEncryptionContext.encrypt(it)
            },
            workDetailsContent = WorkDetailsContent.EMPTY,
            extraSectionContentList = emptyList(),
            customFields = emptyList()
        )
    )

    fun random(
        itemType: ItemType? = null,
        title: String? = null,
        note: String? = null,
        content: ByteArray? = null,
        lastAutofillTime: Long? = null,
        createTime: Long = Clock.System.now().toEpochMilliseconds(),
        modificationTime: Long = createTime,
        pinTime: Long? = null
    ): Item {
        val itemTypeParam = itemType ?: ItemType.Login(
            itemEmail = randomString(),
            itemUsername = randomString(),
            password = randomString().encrypt(TestKeyStoreCrypto),
            websites = emptyList(),
            packageInfoSet = emptySet(),
            primaryTotp = randomString().encrypt(TestKeyStoreCrypto),
            customFields = emptyList(),
            passkeys = emptyList()
        )
        val titleParam = title ?: randomString()
        val noteParam = note ?: randomString()
        val itemContent = if (content != null) {
            TestKeyStoreCrypto.encrypt(PlainByteArray(content))
        } else {
            TestKeyStoreCrypto.encrypt(PlainByteArray(byteArrayOf(0x00)))
        }
        return Item(
            id = ItemId(randomString()),
            userId = UserId(randomString()),
            itemUuid = UUID.randomUUID().toString(),
            revision = Random.nextLong(),
            shareId = ShareId(randomString()),
            itemType = itemTypeParam,
            title = TestKeyStoreCrypto.encrypt(titleParam),
            note = TestKeyStoreCrypto.encrypt(noteParam),
            content = itemContent,
            packageInfoSet = emptySet(),
            state = 0,
            createTime = Instant.fromEpochMilliseconds(createTime),
            modificationTime = Instant.fromEpochMilliseconds(modificationTime),
            lastAutofillTime = lastAutofillTime?.let { Instant.fromEpochMilliseconds(it) }
                .toOption(),
            isPinned = Random.nextBoolean(),
            pinTime = pinTime?.let { Instant.fromEpochMilliseconds(it) }.toOption(),
            itemFlags = ItemFlags(Random.nextInt()),
            shareCount = Random.nextInt(),
            shareType = ShareType.from(Random.nextInt(1, 2))
        )
    }
}
