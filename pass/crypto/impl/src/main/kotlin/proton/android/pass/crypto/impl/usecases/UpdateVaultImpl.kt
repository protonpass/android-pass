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

package proton.android.pass.crypto.impl.usecases

import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedUpdateVaultRequest
import proton.android.pass.crypto.api.usecases.UpdateVault
import proton.pass.domain.key.ShareKey
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

class UpdateVaultImpl @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider
) : UpdateVault {

    override fun createUpdateVaultRequest(
        shareKey: ShareKey,
        body: VaultV1.Vault
    ): EncryptedUpdateVaultRequest {

        val decryptedKey = encryptionContextProvider.withEncryptionContext {
            EncryptionKey(decrypt(shareKey.key))
        }

        val encryptedContent = encryptionContextProvider.withEncryptionContext(decryptedKey) {
            encrypt(body.toByteArray(), EncryptionTag.VaultContent)
        }

        return EncryptedUpdateVaultRequest(
            content = Base64.encodeBase64String(encryptedContent.array),
            contentFormatVersion = CONTENT_FORMAT_VERSION,
            keyRotation = shareKey.rotation
        )
    }

    companion object {
        private const val CONTENT_FORMAT_VERSION = 1
    }
}
