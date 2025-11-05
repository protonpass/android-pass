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

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Test
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.datamodels.api.serializeToProto
import proton.android.pass.domain.ItemContents
import proton.android.pass.test.TestUtils
import proton_pass_item_v1.ItemV1
import kotlin.random.Random
import kotlin.test.assertEquals

class UpdateItemImplTest {
    private val encryptionContextProvider = TestEncryptionContextProvider()

    @Test
    fun canUpdateItem() {
        val instance = UpdateItemImpl(encryptionContextProvider)

        val lastRevision = Random.nextLong()

        val (itemKey, decryptedItemKey) = TestUtils.createItemKey()
        val contents = ItemContents.Note(
            title = TestUtils.randomString(),
            note = TestUtils.randomString(),
            customFields = emptyList()
        )
        val body = instance.createRequest(
            itemKey,
            contents.serializeToProto(encryptionContext = TestEncryptionContext),
            lastRevision
        )

        assertEquals(lastRevision, body.lastRevision)
        assertEquals(itemKey.rotation, body.keyRotation)

        val decodedContents = Base64.decodeBase64(body.content)
        val decryptedContent = encryptionContextProvider.withEncryptionContext(decryptedItemKey) {
            decrypt(EncryptedByteArray(decodedContents), EncryptionTag.ItemContent)
        }

        val asItemContents = ItemV1.Item.parseFrom(decryptedContent)
        assertEquals(contents.title, asItemContents.metadata.name)
        assertEquals(contents.note, asItemContents.metadata.note)
    }
}

