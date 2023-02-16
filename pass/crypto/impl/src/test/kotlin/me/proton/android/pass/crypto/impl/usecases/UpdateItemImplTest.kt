package me.proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.apache.commons.codec.binary.Base64
import org.junit.Test
import proton.android.pass.crypto.api.context.EncryptionTag
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.android.pass.crypto.impl.extensions.serializeToProto
import proton.android.pass.crypto.impl.usecases.UpdateItemImpl
import proton.pass.domain.ItemContents
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
            title = proton.android.pass.test.TestUtils.randomString(),
            note = proton.android.pass.test.TestUtils.randomString()
        )
        val body = instance.createRequest(
            itemKey,
            contents.serializeToProto(),
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

