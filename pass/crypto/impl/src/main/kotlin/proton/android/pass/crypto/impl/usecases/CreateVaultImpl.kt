/*
 * Copyright (c) 2023-2026 Proton AG
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

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.key.domain.encryptAndSignData
import me.proton.core.user.domain.entity.User
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.CreateVault
import proton.android.pass.crypto.api.usecases.CreateVaultOutput
import proton.android.pass.crypto.api.usecases.EncryptedCreateVault
import proton.android.pass.commonrust.api.ItemContentSerializer
import proton.android.pass.crypto.impl.extensions.tryUseKeys
import proton.android.pass.domain.AddressId
import proton.android.pass.domain.VaultProperties
import javax.inject.Inject

class CreateVaultImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val itemContentSerializer: ItemContentSerializer
) : CreateVault {

    override fun createVaultRequest(
        user: User,
        addressId: AddressId,
        vaultMetadata: VaultProperties
    ): CreateVaultOutput {
        val vaultKey = EncryptionKey.generate()
        val vaultContents = itemContentSerializer.serializeVault(vaultMetadata)

        val encryptedVaultKey = user.tryUseKeys("create vault request", cryptoContext) {
            encryptAndSignData(vaultKey.value())
        }

        val encryptedVaultContents = encryptionContextProvider.withEncryptionContext(vaultKey.clone()) {
            encrypt(vaultContents, EncryptionTag.VaultContent)
        }

        return CreateVaultOutput(
            request = EncryptedCreateVault(
                addressId = addressId.id,
                content = Base64.encodeBase64String(encryptedVaultContents.array),
                contentFormatVersion = CONTENT_FORMAT_VERSION,
                encryptedVaultKey = Base64.encodeBase64String(cryptoContext.pgpCrypto.getUnarmored(encryptedVaultKey))
            ),
            shareKey = vaultKey
        )
    }

    companion object {
        const val CONTENT_FORMAT_VERSION = 1
    }
}
