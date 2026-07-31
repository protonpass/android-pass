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
import kotlinx.coroutines.test.runTest
import org.junit.Test

internal class PasswordDirectSuggestionResolverTest {

    private val resolver = PasswordDirectSuggestionResolver()

    @Test
    internal fun `WHEN trusted browser origin is available THEN it is used without reading package cache`() = runTest {
        val result = resolver.resolve(
            callerContext = PasswordCallerContext.Browser(origin = "https://login.example.test"),
            isBrowserOriginPresent = true,
            cachedWebsite = { error("browser suggestions must not read the package cache") }
        )

        assertThat(result).isEqualTo("https://login.example.test")
    }

    @Test
    internal fun `WHEN native caller is available THEN cached website is used`() = runTest {
        val result = resolver.resolve(
            callerContext = PasswordCallerContext.Native(
                packageName = "com.example.credentialapp",
                certificateFingerprints = setOf("certificate")
            ),
            isBrowserOriginPresent = false,
            cachedWebsite = { "https://login.example.test" }
        )

        assertThat(result).isEqualTo("https://login.example.test")
    }

    @Test
    internal fun `WHEN browser origin is present but not trusted THEN no package cache suggestion is used`() = runTest {
        val result = resolver.resolve(
            callerContext = null,
            isBrowserOriginPresent = true,
            cachedWebsite = { error("untrusted browser suggestions must not read the package cache") }
        )

        assertThat(result).isEmpty()
    }

}
