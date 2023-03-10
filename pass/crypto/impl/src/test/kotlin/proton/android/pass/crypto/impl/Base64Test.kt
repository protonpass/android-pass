package proton.android.pass.crypto.impl

import org.apache.commons.codec.binary.Base64
import org.junit.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class Base64Test {

    @Test
    fun worksAsExpected() {
        assertContentEquals(
            Base64.encodeBase64("Kotlin is awesome".encodeToByteArray()),
            "S290bGluIGlzIGF3ZXNvbWU=".encodeToByteArray()
        )
        assertEquals(
            "Kotlin is awesome",
            String(Base64.decodeBase64("S290bGluIGlzIGF3ZXNvbWU="), Charsets.US_ASCII)
        )
    }

    @Test
    fun canEncodeDecode() {
        val input = "this is a test"
        val encoded = Base64.encodeBase64(input.encodeToByteArray())
        val decoded = Base64.decodeBase64(encoded)
        assertContentEquals(input.toByteArray(), decoded)
        assertEquals(input, String(decoded, Charsets.US_ASCII))
    }

}
