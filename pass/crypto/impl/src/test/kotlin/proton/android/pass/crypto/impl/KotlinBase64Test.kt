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

package proton.android.pass.crypto.impl

import org.junit.Test
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(ExperimentalEncodingApi::class)
class KotlinBase64Test {

    @Test
    fun `can encode`() {
        Cases.forEach { (str, encoded) ->
            assertEquals(
                encoded,
                Base64.UrlSafe.encode(str.encodeToByteArray())
            )
            assertEquals(
                encoded,
                Base64.UrlSafe.encode(str.encodeToByteArray())
            )
            assertContentEquals(
                encoded.encodeToByteArray(),
                Base64.UrlSafe.encodeToByteArray(str.encodeToByteArray())
            )
        }
    }

    @Test
    fun `can decode`() {
        Cases.forEach { (str, encoded) ->
            assertEquals(
                str,
                String(Base64.UrlSafe.decode(encoded), Charsets.UTF_8)
            )
            assertContentEquals(
                str.encodeToByteArray(),
                Base64.UrlSafe.decode(encoded)
            )
        }
    }

    @Test
    fun canEncodeDecode() {
        val input = "this is a test"
        val encoded = Base64.encode(input.encodeToByteArray())
        val decoded = Base64.decode(encoded)
        assertContentEquals(input.toByteArray(), decoded)
        assertEquals(input, String(decoded, Charsets.US_ASCII))
    }

    companion object {
        private val Cases = mapOf(
            "Kotlin is awesome" to "S290bGluIGlzIGF3ZXNvbWU=",
            "This! is_:@ t3st {with} rÁndÖm chªrÂcters%&/()" to
                "VGhpcyEgaXNfOkAgdDNzdCB7d2l0aH0gcsOBbmTDlm0gY2jCqnLDgmN0ZXJzJSYvKCk="
        )
    }
}
