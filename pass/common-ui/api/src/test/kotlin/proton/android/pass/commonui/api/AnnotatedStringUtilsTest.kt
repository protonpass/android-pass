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

package proton.android.pass.commonui.api

import junit.framework.TestCase.assertEquals
import org.junit.Test

class AnnotatedStringUtilsTest {

    @Test
    fun `ensureUrlScheme should add https prefix when no scheme is present`() {
        assertEquals("https://example.com", "example.com".ensureUrlScheme())
    }

    @Test
    fun `ensureUrlScheme should not double prefix urls starting with https`() {
        assertEquals("https://example.com", "https://example.com".ensureUrlScheme())
    }

    @Test
    fun `ensureUrlScheme should not double prefix urls starting with http`() {
        assertEquals("http://example.com", "http://example.com".ensureUrlScheme())
    }

    @Test
    fun `ensureUrlScheme should add https prefix to www urls`() {
        assertEquals("https://www.example.com", "www.example.com".ensureUrlScheme())
    }

    @Test
    fun `isPartOfEmail should return true when match is preceded by at sign`() {
        val text = "user@gmail.com"
        val matchStart = text.indexOf("gmail.com")
        assertEquals(true, text.isPartOfEmail(matchStart))
    }

    @Test
    fun `isPartOfEmail should return false when match is not preceded by at sign`() {
        val text = "visit gmail.com"
        val matchStart = text.indexOf("gmail.com")
        assertEquals(false, text.isPartOfEmail(matchStart))
    }

    @Test
    fun `isPartOfEmail should return false when match is at start of string`() {
        val text = "gmail.com"
        assertEquals(false, text.isPartOfEmail(0))
    }

    @Test
    fun `ensureUrlScheme should handle uppercase HTTP scheme`() {
        assertEquals("HTTP://example.com", "HTTP://example.com".ensureUrlScheme())
    }

    @Test
    fun `ensureUrlScheme should handle uppercase HTTPS scheme`() {
        assertEquals("HTTPS://example.com", "HTTPS://example.com".ensureUrlScheme())
    }

    @Test
    fun `lowercaseSchemeAndHost should lowercase scheme and host only`() {
        assertEquals(
            "https://example.com/MyDocument.pdf",
            "HTTPS://EXAMPLE.COM/MyDocument.pdf".lowercaseSchemeAndHost()
        )
    }

    @Test
    fun `lowercaseSchemeAndHost should preserve path and query case`() {
        assertEquals(
            "https://example.com/Path?Key=Value",
            "HTTPS://EXAMPLE.COM/Path?Key=Value".lowercaseSchemeAndHost()
        )
    }

    @Test
    fun `lowercaseSchemeAndHost should handle url without path`() {
        assertEquals(
            "https://example.com",
            "HTTPS://EXAMPLE.COM".lowercaseSchemeAndHost()
        )
    }

    @Test
    fun `lowercaseSchemeAndHost should return unchanged when no scheme`() {
        assertEquals("example.com/Path", "example.com/Path".lowercaseSchemeAndHost())
    }
}
