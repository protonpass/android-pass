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
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.KotlinBase64
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

    @Test
    fun fallbackDecodeCanDecodeUrlSafe() {
        val expected = "subjects?_d=1"
        val input = "c3ViamVjdHM_X2Q9MQ=="

        val decoded = KotlinBase64.decodeBase64(input.encodeToByteArray(), Base64.Mode.UrlSafe)
        assertContentEquals(expected.encodeToByteArray(), decoded)
        assertEquals(expected, String(decoded, Charsets.US_ASCII))
    }

    @Test
    fun fallbackDecodeCanDecodeStandard() {
        val expected = "subjects?_d=1"
        val input = "c3ViamVjdHM/X2Q9MQ=="

        val decoded = KotlinBase64.decodeBase64(input.encodeToByteArray(), Base64.Mode.Standard)
        assertContentEquals(expected.encodeToByteArray(), decoded)
        assertEquals(expected, String(decoded, Charsets.US_ASCII))
    }

}
