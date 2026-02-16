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
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.data.impl.db.entities.ShareKeyEntity

object ShareKeyEntityTestFactory {

    fun create(
        rotation: Long = 1,
        userId: String = "user-id",
        addressId: String = "address-id",
        shareId: String = "share-id",
        key: String = "response-key",
        createTime: Long = 0L,
        symmetricallyEncryptedKey: EncryptedByteArray = FakeEncryptionContext.encrypt(byteArrayOf(1)),
        userKeyId: String = "user-key-id",
        isActive: Boolean = true
    ): ShareKeyEntity = ShareKeyEntity(
        rotation = rotation,
        userId = userId,
        addressId = addressId,
        shareId = shareId,
        key = key,
        createTime = createTime,
        symmetricallyEncryptedKey = symmetricallyEncryptedKey,
        userKeyId = userKeyId,
        isActive = isActive
    )
}
