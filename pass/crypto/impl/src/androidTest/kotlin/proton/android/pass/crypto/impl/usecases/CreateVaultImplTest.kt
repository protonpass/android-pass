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

import me.proton.core.crypto.android.context.AndroidCryptoContext
import me.proton.core.crypto.android.pgp.GOpenPGPCrypto
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.pgp.VerificationStatus
import me.proton.core.key.domain.decryptAndVerifyData
import me.proton.core.key.domain.useKeys
import me.proton.core.user.domain.entity.User
import org.junit.Test
import proton.android.pass.account.fakes.FakeKeyStoreCrypto
import proton.android.pass.commonrust.impl.ItemContentSerializerImpl
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.EncryptedCreateVault
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.crypto.impl.CoreUserKeys
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.VaultProperties
import proton.android.pass.test.UserAddressTestFactory
import proton.android.pass.test.domain.UserTestFactory
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class CreateVaultImplTest {
    private val cryptoContext: CryptoContext = AndroidCryptoContext(
        keyStoreCrypto = FakeKeyStoreCrypto,
        pgpCrypto = GOpenPGPCrypto()
    )

    private val encryptionContextProvider = FakeEncryptionContextProvider()
    private val itemContentSerializer = ItemContentSerializerImpl()

    @Test
    fun canCreateVault() {
        val userAddress = UserAddressTestFactory.createUserAddress(cryptoContext)
        val vaultName = Utils.generatePassphrase()
        val vaultDescription = Utils.generatePassphrase()
        val user = UserTestFactory.createWithKeys()
        val vaultMetadata = VaultProperties(
            name = vaultName,
            description = vaultDescription,
            icon = ShareIcon.Icon1,
            color = ShareColor.Color1
        )

        val instance = CreateVaultImpl(cryptoContext, encryptionContextProvider, itemContentSerializer)
        val (request, vaultKey) = instance.createVaultRequest(
            CoreUserKeys(user, cryptoContext),
            userAddress.addressId,
            vaultMetadata
        )

        validateVaultKey(user, request, vaultKey)
        validateVaultMetadata(vaultMetadata, request, vaultKey)
    }

    private fun validateVaultKey(
        user: User,
        request: EncryptedCreateVault,
        vaultKey: EncryptionKey
    ) {
        val decryptedVaultKey = user.useKeys(cryptoContext) {
            val decoded = cryptoContext.pgpCrypto.getBase64Decoded(request.encryptedVaultKey)
            val armored = cryptoContext.pgpCrypto.getArmored(decoded)
            decryptAndVerifyData(armored)
        }

        assertEquals(decryptedVaultKey.status, VerificationStatus.Success)
        assertContentEquals(vaultKey.value(), decryptedVaultKey.data)
    }

    private fun validateVaultMetadata(
        metadata: VaultProperties,
        request: EncryptedCreateVault,
        vaultKey: EncryptionKey
    ) {
        val decodedRequestContent = cryptoContext.pgpCrypto.getBase64Decoded(request.content)
        val decrypted = encryptionContextProvider.withEncryptionContext(vaultKey) {
            decrypt(EncryptedByteArray(decodedRequestContent), EncryptionTag.VaultContent)
        }

        val parsed = itemContentSerializer.deserializeVault(decrypted)
        assertEquals(metadata.name, parsed.name)
        assertEquals(metadata.description, parsed.description)
    }
}

