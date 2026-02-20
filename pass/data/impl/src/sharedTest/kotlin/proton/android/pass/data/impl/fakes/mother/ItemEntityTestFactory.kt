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

package proton.android.pass.data.impl.fakes.mother

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.account.fakes.FakeKeyStoreCrypto
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.android.pass.domain.ItemStateValues

object ItemEntityTestFactory {

    fun create(
        id: String = "item-id",
        userId: String = "user-id",
        addressId: String = "address-id",
        shareId: String = "share-id",
        folderId: String? = null,
        revision: Long = 1L,
        contentFormatVersion: Int = 1,
        keyRotation: Long = 1L,
        content: String = "",
        key: String? = null,
        state: Int = ItemStateValues.ACTIVE,
        itemType: Int = 1,
        aliasEmail: String? = null,
        createTime: Long = 1000L,
        modifyTime: Long = 1000L,
        lastUsedTime: Long? = null,
        encryptedTitle: EncryptedString = FakeKeyStoreCrypto.encrypt("title"),
        encryptedNote: EncryptedString = FakeKeyStoreCrypto.encrypt("note"),
        encryptedContent: EncryptedByteArray = FakeKeyStoreCrypto.encrypt(
            me.proton.core.crypto.common.keystore.PlainByteArray(byteArrayOf())
        ),
        encryptedKey: EncryptedByteArray? = null,
        isPinned: Boolean = false,
        pinTime: Long? = null,
        flags: Int = 0,
        shareCount: Int = 0,
        hasTotp: Boolean = false,
        hasPasskeys: Boolean = false
    ): ItemEntity = ItemEntity(
        id = id,
        userId = userId,
        addressId = addressId,
        shareId = shareId,
        folderId = folderId,
        revision = revision,
        contentFormatVersion = contentFormatVersion,
        keyRotation = keyRotation,
        content = content,
        key = key,
        state = state,
        itemType = itemType,
        aliasEmail = aliasEmail,
        createTime = createTime,
        modifyTime = modifyTime,
        lastUsedTime = lastUsedTime,
        encryptedTitle = encryptedTitle,
        encryptedNote = encryptedNote,
        encryptedContent = encryptedContent,
        encryptedKey = encryptedKey,
        isPinned = isPinned,
        pinTime = pinTime,
        flags = flags,
        shareCount = shareCount,
        hasTotp = hasTotp,
        hasPasskeys = hasPasskeys
    )
}
