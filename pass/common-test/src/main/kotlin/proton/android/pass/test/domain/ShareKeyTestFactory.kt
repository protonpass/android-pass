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

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.domain.key.ShareKey

@Suppress("UnderscoresInNumericLiterals")
object ShareKeyTestFactory {
    fun createPrivate(): ShareKey = ShareKey(
        rotation = 1,
        key = EncryptedByteArray(byteArrayOf(1, 2, 3)),
        responseKey = "base64ShareKey",
        createTime = 12345678,
        isActive = true,
        userKeyId = "userKeyId"
    )

    fun create(): Pair<ShareKey, EncryptionKey> {
        val key = EncryptionKey.generate()
        return ShareKey(
            rotation = 1,
            key = FakeEncryptionContext.encrypt(key.value()),
            responseKey = Base64.encodeBase64String(key.value()),
            createTime = 123_456_789,
            isActive = true,
            userKeyId = "userKeyId"
        ) to key
    }
}
