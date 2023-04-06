package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Test
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.pass.domain.ItemContents
import proton_pass_item_v1.ItemV1
import kotlin.test.assertEquals

class MigrateItemImplTest {

    private val encryptionContextProvider = TestEncryptionContextProvider()
    private val createItem = CreateItemImpl(encryptionContextProvider)

    @Test
    fun testCanMigrateItem() {
        // GIVEN
        val contents = ItemContents.Note(
            title = proton.android.pass.test.TestUtils.randomString(),
            note = proton.android.pass.test.TestUtils.randomString()
        )
        val (sourceShareKey, _) = TestUtils.createShareKey()
        val (destinationShareKey, decryptedDestinationShareKey) = TestUtils.createShareKey()
        val item = createItem.create(sourceShareKey, contents)

        val encryptedItemContents = Base64.decodeBase64(item.request.content)

        // WHEN
        val instance = MigrateItemImpl(encryptionContextProvider)
        val output = instance.migrate(
            destinationKey = destinationShareKey,
            encryptedItemContents = EncryptedByteArray(encryptedItemContents),
            contentFormatVersion = item.request.contentFormatVersion
        )

        // THEN
        assertEquals(output.contentFormatVersion, item.request.contentFormatVersion)
        assertEquals(output.keyRotation, destinationShareKey.rotation)

        val decryptedItemKey =
            encryptionContextProvider.withEncryptionContext(decryptedDestinationShareKey) {
                EncryptionKey(decrypt(EncryptedByteArray(Base64.decodeBase64(output.itemKey))))
            }

        val decryptedContent = encryptionContextProvider.withEncryptionContext(decryptedItemKey) {
            decrypt(EncryptedByteArray(Base64.decodeBase64(output.content)))
        }

        val parsed = ItemV1.Item.parseFrom(decryptedContent)
        assertEquals(parsed.metadata.name, contents.title)
        assertEquals(parsed.metadata.note, contents.note)
    }
}
