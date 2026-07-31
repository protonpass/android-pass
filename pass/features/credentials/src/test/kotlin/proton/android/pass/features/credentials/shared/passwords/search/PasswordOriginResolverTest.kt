/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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

package proton.android.pass.features.credentials.shared.passwords.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test

internal class PasswordOriginResolverTest {

    private val resolver = PasswordOriginResolver()

    @Test
    internal fun `canonicalizeLoginUrl reduces https URL path query and fragment to host origin`() {
        assertThat(resolver.canonicalizeLoginUrl("https://Login.Example.Test/login?source=app#section"))
            .isEqualTo("https://login.example.test")
    }

    @Test
    internal fun `canonicalizeLoginUrl preserves a distinct lookalike host instead of matching trusted host`() {
        assertThat(resolver.canonicalizeLoginUrl("https://login.example.test.attacker.example.test/login"))
            .isEqualTo("https://login.example.test.attacker.example.test")
    }

    @Test
    internal fun `canonicalizeLoginUrl rejects non https scheme`() {
        assertThat(resolver.canonicalizeLoginUrl("http://login.example.test/login")).isNull()
    }

    @Test
    internal fun `canonicalizeLoginUrl rejects URL with user info`() {
        assertThat(resolver.canonicalizeLoginUrl("https://attacker@login.example.test/login")).isNull()
    }

    @Test
    internal fun `canonicalizeLoginUrl rejects URL with explicit port`() {
        assertThat(resolver.canonicalizeLoginUrl("https://login.example.test:443/login")).isNull()
    }

    @Test
    internal fun `canonicalizeLoginUrl rejects IPv4 literal host`() {
        assertThat(resolver.canonicalizeLoginUrl("https://127.0.0.1/login")).isNull()
    }

    @Test
    internal fun `canonicalizeLoginUrl rejects bracketed IPv6 literal host`() {
        assertThat(resolver.canonicalizeLoginUrl("https://[::1]/login")).isNull()
    }

    @Test
    internal fun `canonicalizeLoginUrl rejects URL with no host`() {
        assertThat(resolver.canonicalizeLoginUrl("https:///login")).isNull()
    }

    @Test
    internal fun `canonicalizeLoginUrl rejects malformed URL`() {
        assertThat(resolver.canonicalizeLoginUrl("https://login.example .test/login")).isNull()
    }
}
