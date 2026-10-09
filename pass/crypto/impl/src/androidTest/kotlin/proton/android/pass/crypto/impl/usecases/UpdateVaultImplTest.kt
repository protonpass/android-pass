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

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Test
import proton.android.pass.commonrust.impl.ItemContentSerializerImpl
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.VaultProperties
import proton.android.pass.test.domain.ShareKeyTestFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateVaultImplTest {
    private val encryptionContextProvider = FakeEncryptionContextProvider()
    private val itemContentSerializer = ItemContentSerializerImpl()

    @Test
    fun canUpdateVault() {
        val instance = UpdateVaultImpl(encryptionContextProvider, itemContentSerializer)
        val (shareKey, decryptedShareKey) = ShareKeyTestFactory.create()
        val body = VaultProperties(
            name = "Vault name",
            description = "Vault description",
            icon = ShareIcon.Icon1,
            color = ShareColor.Color1
        )

        val result = instance.createUpdateVaultRequest(shareKey, null, body)

        assertEquals(shareKey.rotation, result.keyRotation)

        val decryptedContent = encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
            decrypt(EncryptedByteArray(Base64.decodeBase64(result.content)), EncryptionTag.VaultContent)
        }
        val asVaultData = itemContentSerializer.deserializeVault(decryptedContent)
        assertEquals(body.name, asVaultData.name)
        assertEquals(body.description, asVaultData.description)
    }

    @Test
    fun updateKeepsUnknownFieldsFromOriginalContent() {
        val instance = UpdateVaultImpl(encryptionContextProvider, itemContentSerializer)
        val (shareKey, decryptedShareKey) = ShareKeyTestFactory.create()
        val original = VaultProperties(
            name = "Original name",
            description = "Original description",
            icon = ShareIcon.Icon1,
            color = ShareColor.Color1
        )
        val originalContent = itemContentSerializer.serializeVault(original) + UnknownProtobufField.bytes
        val updated = original.copy(name = "Updated name")

        val result = instance.createUpdateVaultRequest(shareKey, originalContent, updated)

        val decryptedContent = encryptionContextProvider.withEncryptionContext(decryptedShareKey) {
            decrypt(EncryptedByteArray(Base64.decodeBase64(result.content)), EncryptionTag.VaultContent)
        }
        val asVaultData = itemContentSerializer.deserializeVault(decryptedContent)
        assertEquals("Updated name", asVaultData.name)
        assertEquals(original.description, asVaultData.description)
        assertTrue(UnknownProtobufField.isContainedIn(decryptedContent))
    }
}
