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
import proton.android.pass.domain.Flags
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType
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
            flags = Flags(0),
            shareCount = 0,
            shareType = ShareType.Vault
        )
    }

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
            flags = Flags(Random.nextInt()),
            shareCount = Random.nextInt(),
            shareType = ShareType.from(Random.nextInt(1, 2))
        )
    }
}
