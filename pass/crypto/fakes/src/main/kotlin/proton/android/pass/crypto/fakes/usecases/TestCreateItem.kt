package proton.android.pass.crypto.fakes.usecases

import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.usecases.CreateItem
import proton.android.pass.crypto.api.usecases.CreateItemPayload
import proton.android.pass.crypto.api.usecases.EncryptedCreateItem
import proton.android.pass.crypto.fakes.context.TestEncryptionContext
import proton.pass.domain.ItemContents
import proton.pass.domain.key.ShareKey

class TestCreateItem : CreateItem {

    private var payload: CreateItemPayload? = null

    fun setPayload(value: CreateItemPayload) {
        payload = value
    }

    override fun create(
        shareKey: ShareKey,
        itemContents: ItemContents
    ): CreateItemPayload = payload ?: throw IllegalStateException("payload is not set")

    companion object {
        fun createPayload(): CreateItemPayload {
            val key = EncryptionKey.generate()
            return CreateItemPayload(
                request = EncryptedCreateItem(
                    contentFormatVersion = 1,
                    content = TestKeyStoreCrypto.encrypt("content"),
                    keyRotation = 1,
                    itemKey = Base64.encodeBase64String(TestEncryptionContext.encrypt(key.value()).array)
                ),
                itemKey = key
            )
        }
    }
}
