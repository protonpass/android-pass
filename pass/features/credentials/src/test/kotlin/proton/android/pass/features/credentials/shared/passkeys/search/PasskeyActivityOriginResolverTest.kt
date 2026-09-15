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

package proton.android.pass.features.credentials.shared.passkeys.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PasskeyActivityOriginResolverTest {

    // --- extractRpIdFromJson (GET / authentication shape: top-level "rpId") ---

    @Test
    fun `extractRpIdFromJson returns rpId from GET-shaped JSON`() {
        val json = """{"rpId":"example.com","challenge":"abc123","allowCredentials":[]}"""
        assertEquals("example.com", PasskeyActivityOriginResolver.extractRpIdFromJson(json))
    }

    @Test
    fun `extractRpIdFromJson returns null when rpId key is absent`() {
        val json = """{"challenge":"abc123","allowCredentials":[]}"""
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromJson(json))
    }

    @Test
    fun `extractRpIdFromJson returns null for CREATE-shaped JSON with no top-level rpId`() {
        val json = """{"rp":{"id":"example.com","name":"Example"},"challenge":"abc123"}"""
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromJson(json))
    }

    @Test
    fun `extractRpIdFromJson returns null for empty rpId value`() {
        val json = """{"rpId":"","challenge":"abc123"}"""
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromJson(json))
    }

    @Test
    fun `extractRpIdFromJson returns null for malformed JSON`() {
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromJson("not json at all"))
    }

    @Test
    fun `extractRpIdFromJson returns null for empty string`() {
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromJson(""))
    }

    // --- extractRpIdFromCreateJson (CREATE / registration shape: "rp.id") ---

    @Test
    fun `extractRpIdFromCreateJson returns rp id from CREATE-shaped JSON`() {
        val json = """
            {"rp":{"id":"example.com","name":"Example"},"user":{"id":"user1","name":"alice"},"challenge":"abc"}
        """.trimIndent()
        assertEquals("example.com", PasskeyActivityOriginResolver.extractRpIdFromCreateJson(json))
    }

    @Test
    fun `extractRpIdFromCreateJson returns null when rp object is absent`() {
        val json = """{"rpId":"example.com","challenge":"abc123"}"""
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromCreateJson(json))
    }

    @Test
    fun `extractRpIdFromCreateJson returns null when rp id field is absent`() {
        val json = """{"rp":{"name":"Example"},"challenge":"abc123"}"""
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromCreateJson(json))
    }

    @Test
    fun `extractRpIdFromCreateJson returns null for empty rp id value`() {
        val json = """{"rp":{"id":"","name":"Example"},"challenge":"abc123"}"""
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromCreateJson(json))
    }

    @Test
    fun `extractRpIdFromCreateJson returns null for malformed JSON`() {
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromCreateJson("not json at all"))
    }

    @Test
    fun `extractRpIdFromCreateJson returns null for empty string`() {
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromCreateJson(""))
    }

    @Test
    fun `extractRpIdFromCreateJson does not confuse GET-shaped rpId at top level`() {
        val json = """{"rpId":"should-not-be-read.com","challenge":"abc123"}"""
        assertNull(PasskeyActivityOriginResolver.extractRpIdFromCreateJson(json))
    }
}
