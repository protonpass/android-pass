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

package proton.android.pass.data.impl.crypto

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import javax.inject.Inject

interface ReencryptShareContents {
    operator fun invoke(contents: String?, key: EncryptionKey): EncryptedByteArray?
}

class ReencryptShareContentsImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : ReencryptShareContents {
    override fun invoke(contents: String?, key: EncryptionKey): EncryptedByteArray? {
        if (contents == null) return null

        val decoded = Base64.decodeBase64(contents)
        val decrypted = encryptionContextProvider.withEncryptionContext(key) {
            decrypt(EncryptedByteArray(decoded), EncryptionTag.VaultContent)
        }

        // Using our default key
        return encryptionContextProvider.withEncryptionContext { encrypt(decrypted) }
    }
}
