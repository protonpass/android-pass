package proton.android.pass.crypto.impl.usecases

import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.usecases.EncryptedItemKey
import proton.android.pass.crypto.impl.context.TestEncryptionContextProvider
import proton.pass.domain.key.ShareKey
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@Suppress("UnderscoresInNumericLiterals")
class OpenItemKeyImplTest {

    private lateinit var encryptionContextProvider: EncryptionContextProvider
    private lateinit var instance: OpenItemKeyImpl

    @Before
    fun setup() {
        encryptionContextProvider = TestEncryptionContextProvider(EncryptionKey.generate())
        instance = OpenItemKeyImpl(encryptionContextProvider)
    }

    @Test
    fun canOpenItemKey() {
        val res = instance.invoke(
            shareKey = getShareKey(),
            key = EncryptedItemKey(key = encryptedItemKey, keyRotation = KEY_ROTATION)
        )

        assertEquals(KEY_ROTATION, res.rotation)
        assertEquals(encryptedItemKey, res.responseKey)

        encryptionContextProvider.withEncryptionContext {
            val decrypted = decrypt(res.key)
            val expectedDecoded = Base64.decodeBase64(itemKeyBase64)

            assertContentEquals(expectedDecoded, decrypted)
        }
    }


    private fun getShareKey(): ShareKey {
        val decodedShareKey = Base64.decodeBase64(shareKeyBase64)
        return ShareKey(
            rotation = KEY_ROTATION,
            key = encryptionContextProvider.withEncryptionContext { encrypt(decodedShareKey) },
            responseKey = OpenItemImplTest.shareKeyBase64,
            createTime = 1664195804
        )
    }

    companion object {
        private const val KEY_ROTATION = 123L
        private const val shareKeyBase64 = "jx04DqGD1OG+PAHLO9DaktCF1/EopRfTfV3uIl121HI="
        private const val itemKeyBase64 = "atlecrF7Odsa56d6BTZY/cW88IbmnTKEbOUR+jaGXdg="

        private const val encryptedItemKey =
            "OJDk553MI8tbyWpLqxDlBhv0kmL/BceqUacMMaazntOP9xZg3f2Q9bkwY7dw9mitkpNVdBOMxUWezgza"
    }

}
