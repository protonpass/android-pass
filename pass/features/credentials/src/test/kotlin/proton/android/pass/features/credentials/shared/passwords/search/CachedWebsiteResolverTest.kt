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

package proton.android.pass.features.credentials.shared.passwords.search

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.domain.assetlink.AssetLink

internal class CachedWebsiteResolverTest {

    private val resolver = CachedWebsiteResolver()

    @Test
    fun `returns first cached website`() {
        val result = resolver(
            listOf(
                assetLink("https://first.example"),
                assetLink("https://second.example")
            )
        )

        assertThat(result).isEqualTo("https://first.example")
    }

    @Test
    fun `returns empty string when cache is empty`() {
        assertThat(resolver(emptyList())).isEmpty()
    }

    private fun assetLink(website: String) = AssetLink(website, emptySet())

}
