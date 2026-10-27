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
import proton.android.pass.domain.ItemContents
import proton.android.pass.test.StringTestFactory
import proton.android.pass.test.domain.ItemKeyTestFactory
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdateItemImplTest {
    private val encryptionContextProvider = FakeEncryptionContextProvider()
    private val itemContentSerializer = ItemContentSerializerImpl()

    @Test
    fun canUpdateItem() {
        val instance = UpdateItemImpl(encryptionContextProvider)

        val lastRevision = Random.nextLong()

        val (itemKey, decryptedItemKey) = ItemKeyTestFactory.create()
        val contents = ItemContents.Note(
            title = StringTestFactory.randomString(),
            note = StringTestFactory.randomString(),
            customFields = emptyList()
        )
        val serialized = encryptionContextProvider.withEncryptionContext {
            itemContentSerializer.serializeItem(contents, this)
        }
        val body = instance.createRequest(
            itemKey,
            serialized,
            lastRevision
        )

        assertEquals(lastRevision, body.lastRevision)
        assertEquals(itemKey.rotation, body.keyRotation)

        val decodedContents = Base64.decodeBase64(body.content)
        val decryptedContent = encryptionContextProvider.withEncryptionContext(decryptedItemKey) {
            decrypt(EncryptedByteArray(decodedContents), EncryptionTag.ItemContent)
        }

        val asItemContents = encryptionContextProvider.withEncryptionContext {
            itemContentSerializer.deserializeItem(decryptedContent, this)
        }
        assertEquals(contents.title, asItemContents.title)
        assertEquals(contents.note, asItemContents.note)
    }

    @Test
    fun updateKeepsUnknownFieldsFromOriginalContent() {
        val instance = UpdateItemImpl(encryptionContextProvider)
        val (itemKey, decryptedItemKey) = ItemKeyTestFactory.create()
        val original = ItemContents.Note(
            title = StringTestFactory.randomString(),
            note = StringTestFactory.randomString(),
            customFields = emptyList()
        )
        val originalContent = encryptionContextProvider.withEncryptionContext {
            itemContentSerializer.serializeItem(original, this)
        } + UnknownProtobufField.bytes
        val updated = original.copy(note = "")

        val updatedContent = encryptionContextProvider.withEncryptionContext {
            itemContentSerializer.performItemUpdate(originalContent, updated, this)
        }
        val body = instance.createRequest(
            itemKey,
            updatedContent,
            Random.nextLong()
        )

        val decryptedContent = encryptionContextProvider.withEncryptionContext(decryptedItemKey) {
            decrypt(EncryptedByteArray(Base64.decodeBase64(body.content)), EncryptionTag.ItemContent)
        }
        val asItemContents = encryptionContextProvider.withEncryptionContext {
            itemContentSerializer.deserializeItem(decryptedContent, this)
        }
        assertEquals(original.title, asItemContents.title)
        assertEquals("", asItemContents.note)
        assertTrue(UnknownProtobufField.isContainedIn(decryptedContent))
    }
}
