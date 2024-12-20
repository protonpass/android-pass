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

import com.google.common.truth.Truth.assertThat
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.api.usecases.CreateItemPayload
import proton.android.pass.crypto.api.usecases.ItemKeyWithRotation
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.android.pass.domain.ItemContents

internal class MigrateItemsImplTest {

    private val encryptionContextProvider = TestEncryptionContextProvider()
    private val createItem = CreateItemImpl(encryptionContextProvider)

    private lateinit var migrateItem: MigrateItem

    @Before
    internal fun setUp() {
        migrateItem = MigrateItemImpl(encryptionContextProvider)
    }

    @Test
    internal fun `WHEN migrating an item THEN item keys should be migrated`() {
        val sourceShareKey = TestUtils.createShareKey().first
        val itemContents = ItemContents.Note(
            title = proton.android.pass.test.TestUtils.randomString(),
            note = proton.android.pass.test.TestUtils.randomString()
        )
        val item: CreateItemPayload = createItem.create(sourceShareKey, itemContents)
        val (destinationShareKey, decryptedDestinationShareKey) = TestUtils.createShareKey()
        val encryptedItemKey = encryptionContextProvider.withEncryptionContext {
            encrypt(item.request.itemKey.toByteArray())
        }
        val itemKeys = listOf(
            ItemKeyWithRotation(
                itemKey = encryptedItemKey,
                keyRotation = item.request.keyRotation
            )
        )

        val encryptedItemKeys = migrateItem.migrate(destinationShareKey, itemKeys)

        assertThat(encryptedItemKeys).hasSize(1)
        val migratedItemKey = encryptedItemKeys.first()
        assertThat(migratedItemKey.keyRotation).isEqualTo(item.request.keyRotation)

        val decryptedKey = decryptItemKey(
            encryptionKey = decryptedDestinationShareKey,
            encryptedItemKey = migratedItemKey.itemKey
        )

        val expectedKey = Base64.encodeBase64String(item.request.itemKey.toByteArray())
        val actualKey = Base64.encodeBase64String(decryptedKey)
        assertThat(actualKey).isEqualTo(expectedKey)
    }

    private fun decryptItemKey(encryptionKey: EncryptionKey, encryptedItemKey: EncryptedByteArray): ByteArray =
        encryptionContextProvider.withEncryptionContext(encryptionKey) {
            decrypt(encryptedItemKey, EncryptionTag.ItemKey)
        }
}
