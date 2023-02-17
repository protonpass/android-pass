package proton.android.pass.test.domain

import kotlinx.datetime.Clock
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.crypto.common.keystore.KeyStoreCrypto
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.crypto.common.keystore.encrypt
import proton.android.pass.common.api.None
import proton.android.pass.test.TestUtils.randomString
import proton.android.pass.test.crypto.TestKeyStoreCrypto
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ItemType
import proton.pass.domain.ShareId
import kotlin.random.Random

object TestItem {

    fun create(
        itemType: ItemType = ItemType.Password,
        allowedPackageNames: List<String> = emptyList(),
        keyStoreCrypto: KeyStoreCrypto? = null
    ): Item {
        val title = "item-title"
        val note = "item-note"
        val now = Clock.System.now()
        return Item(
            id = ItemId(id = "item-id"),
            revision = 0,
            shareId = ShareId(id = "share-id"),
            itemType = itemType,
            title = keyStoreCrypto?.let { title.encrypt(it) } ?: title,
            note = keyStoreCrypto?.let { note.encrypt(it) } ?: note,
            content = EncryptedByteArray(byteArrayOf()),
            allowedPackageNames = allowedPackageNames,
            modificationTime = now,
            createTime = now,
            lastAutofillTime = None
        )
    }

    fun random(
        itemType: ItemType? = null,
        title: String? = null,
        note: String? = null,
        content: ByteArray? = null
    ): Item {
        val itemTypeParam = itemType ?: ItemType.Login(
            username = randomString(),
            password = randomString().encrypt(TestKeyStoreCrypto),
            websites = emptyList(),
            packageNames = emptyList(),
            primaryTotp = randomString()
        )
        val titleParam = title ?: randomString()
        val noteParam = note ?: randomString()
        val itemContent = if (content != null) {
            TestKeyStoreCrypto.encrypt(PlainByteArray(content))
        } else {
            TestKeyStoreCrypto.encrypt(PlainByteArray(byteArrayOf(0x00)))
        }
        val now = Clock.System.now()
        return Item(
            id = ItemId(randomString()),
            revision = Random.nextLong(),
            shareId = ShareId(randomString()),
            itemType = itemTypeParam,
            title = TestKeyStoreCrypto.encrypt(titleParam),
            note = TestKeyStoreCrypto.encrypt(noteParam),
            content = itemContent,
            allowedPackageNames = emptyList(),
            createTime = now,
            modificationTime = now,
            lastAutofillTime = None,
        )
    }
}
