/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.log.impl

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PrivacySanitizerImplTest {

    private val sanitizer = PrivacySanitizerImpl()

    @Test
    fun `sanitize should redact simple email addresses`() {
        val input = "User email is john.doe@proton.me in the logs"
        val expected = "User email is [EMAIL_REDACTED] in the logs"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact multiple emails in same message`() {
        val input = "Contact john.doe@proton.me or john.doe@proton.me"
        val expected = "Contact [EMAIL_REDACTED] or [EMAIL_REDACTED]"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact complex email formats`() {
        val input = "Send to john.doe+tag@proton.me"
        val expected = "Send to [EMAIL_REDACTED]"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact URL-encoded emails`() {
        val input = "Query param Name=john.doe%40proton.me found"
        val expected = "Query param Name=[EMAIL_REDACTED] found"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact emails in full API URLs`() {
        val input = "https://pass-api.proton.me/core/v4/users/availableExternal?Name=john.doe%40proton.me"
        val expected = "https://pass-api.proton.me/core/v4/users/availableExternal?Name=[EMAIL_REDACTED]"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact both encoded and plain emails in same message`() {
        val input = "Checking john.doe%40proton.me and john.doe@proton.me simultaneously"
        val expected = "Checking [EMAIL_REDACTED] and [EMAIL_REDACTED] simultaneously"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact emails with mixed case in URL encoding`() {
        val input = "Email: JoHn.DoE%40PrOtOn.ME in mixed case"
        val expected = "Email: [EMAIL_REDACTED] in mixed case"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should preserve 88-char IDs with ellipsis masking`() {
        val id = "a".repeat(88)
        val input = "ItemID: $id found"
        val expected = "ItemID: aaaa…aaaa found"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should handle slashes with IDs`() {
        val id = "b".repeat(88)
        val input = "Path: /share/$id/item"
        val expected = "Path: /share/bbbb…bbbb/item"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should not modify normal text without PII`() {
        val input = "Loading items from vault successfully"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(input)
    }

    @Test
    fun `sanitize should handle empty string`() {
        val result = sanitizer.sanitize("")

        assertThat(result).isEqualTo("")
    }

    @Test
    fun `sanitize should handle email-like but invalid patterns`() {
        val input = "Not an email: john.doe@, @proton.me, proton.me"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(input)
    }

    @Test
    fun `sanitize should handle complex URL with multiple PII patterns`() {
        val id = "c".repeat(88)
        val input = "POST /api/$id/send?email=john.doe%40proton.me&confirm=john.doe@proton.me (auth: token123)"
        val result = sanitizer.sanitize(input)

        assertThat(result.contains("[EMAIL_REDACTED]")).isTrue()
        assertThat(result.contains("cccc…cccc")).isTrue()
        assertThat(result.contains("john.doe%40proton.me")).isFalse()
        assertThat(result.contains("john.doe@proton.me")).isFalse()
    }

    @Test
    fun `sanitize should redact bearer tokens`() {
        val input = "GET /api (auth Bearer eyJhbGciOiJSU0EtT0FFUC0yNTYiLCJlbmMiOiJBMjU2R0NNIn0.wTDI61zi9yrk)"
        val expected = "GET /api (auth Bearer [REDACTED])"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact long bearer tokens with dots`() {
        val input = "auth Bearer eyJp.QTI1NkdDTUtXIi.jpMl1_WgyEYQIPzEBJ0CaGdLlSszo8FF1eJY.SbQizbKd"
        val expected = "auth Bearer [REDACTED]"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should handle bearer token case insensitively`() {
        val input = "Authorization: bearer abc123def456"
        val expected = "Authorization: Bearer [REDACTED]"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should handle bearer token with equals padding`() {
        val input = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dGVzdA=="
        val expected = "Bearer [REDACTED]"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact Domain query parameter in image logo URL`() {
        val input = "https://pass-api.proton.me/core/v4/images/logo?Domain=my.domain.test&Size=64"
        val expected = "https://pass-api.proton.me/core/v4/images/logo?Domain=[DOMAIN_REDACTED]&Size=64"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact Domain query parameter when not first param`() {
        val input = "https://example.com/api?Size=64&Domain=sensitive.domain.com&Mode=light"
        val expected = "https://example.com/api?Size=64&Domain=[DOMAIN_REDACTED]&Mode=light"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact Domain query parameter case insensitively`() {
        val input = "https://example.com/api?domain=secret.site.org"
        val expected = "https://example.com/api?domain=[DOMAIN_REDACTED]"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should redact multiple Domain query parameters in same message`() {
        val input = "Fetching ?Domain=first.com and ?Domain=second.com"
        val expected = "Fetching ?Domain=[DOMAIN_REDACTED] and ?Domain=[DOMAIN_REDACTED]"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `sanitize should mask share IDs with 4 chars on each side`() {
        val shareId = "a".repeat(84) + "VA=="
        val input = "Share ID: $shareId found"
        val expected = "Share ID: aaaa…VA== found"

        val result = sanitizer.sanitize(input)

        assertThat(result).isEqualTo(expected)
    }
}
