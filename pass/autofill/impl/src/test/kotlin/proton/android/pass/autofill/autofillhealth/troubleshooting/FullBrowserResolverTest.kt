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

package proton.android.pass.autofill.autofillhealth.troubleshooting

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.autofillhealth.troubleshooting.data.FullBrowserResolver

class FullBrowserResolverTest {

    @Test
    fun `accepts a filter handling both http and https for any host`() {
        val isFullBrowser = FullBrowserResolver.isFullBrowser(
            hasAuthorities = false,
            schemes = setOf("http", "https")
        )

        assertThat(isFullBrowser).isTrue()
    }

    @Test
    fun `rejects a filter restricted to specific hosts`() {
        val isFullBrowser = FullBrowserResolver.isFullBrowser(
            hasAuthorities = true,
            schemes = setOf("http", "https")
        )

        assertThat(isFullBrowser).isFalse()
    }

    @Test
    fun `rejects a filter handling https only`() {
        val isFullBrowser = FullBrowserResolver.isFullBrowser(
            hasAuthorities = false,
            schemes = setOf("https")
        )

        assertThat(isFullBrowser).isFalse()
    }

    @Test
    fun `rejects a filter handling http only`() {
        val isFullBrowser = FullBrowserResolver.isFullBrowser(
            hasAuthorities = false,
            schemes = setOf("http")
        )

        assertThat(isFullBrowser).isFalse()
    }

    @Test
    fun `rejects a filter declaring no web scheme`() {
        val isFullBrowser = FullBrowserResolver.isFullBrowser(
            hasAuthorities = false,
            schemes = emptySet()
        )

        assertThat(isFullBrowser).isFalse()
    }
}
