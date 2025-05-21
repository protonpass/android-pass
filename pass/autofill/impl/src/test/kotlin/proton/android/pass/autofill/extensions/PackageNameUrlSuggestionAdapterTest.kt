/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.autofill.extensions

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.BROWSERS
import proton.android.pass.autofill.api.suggestions.SuggestionSource
import proton.android.pass.domain.entity.PackageName

class PackageNameUrlSuggestionAdapterTest {

    @Test
    fun `adapt returns WithUrl when packageName is not a browser and url is not blank`() {
        val packageName = PackageName("com.example.app")
        val url = "https://example.com"

        val result = PackageNameUrlSuggestionAdapterImpl().adapt(packageName, url)

        assertThat(result).isInstanceOf(SuggestionSource.WithUrl::class.java)
        assertThat((result as SuggestionSource.WithUrl).url).isEqualTo(url)
    }

    @Test
    fun `adapt returns WithPackageName when packageName is not a browser and url is blank`() {
        val packageName = PackageName("com.example.app")
        val url = ""

        val result = PackageNameUrlSuggestionAdapterImpl().adapt(packageName, url)

        assertThat(result).isInstanceOf(SuggestionSource.WithPackageName::class.java)
        assertThat((result as SuggestionSource.WithPackageName).packageName).isEqualTo(packageName.value)
    }

    @Test
    fun `adapt returns WithUrl when packageName is a browser and url is not blank`() {
        val packageName = PackageName(BROWSERS.first())
        val url = "https://example.com"

        val result = PackageNameUrlSuggestionAdapterImpl().adapt(packageName, url)

        assertThat(result).isInstanceOf(SuggestionSource.WithUrl::class.java)
        assertThat((result as SuggestionSource.WithUrl).url).isEqualTo(url)
    }

    @Test
    fun `adapt returns WithUrl when packageName is a browser and url is blank`() {
        val packageName = PackageName(BROWSERS.first())
        val url = ""

        val result = PackageNameUrlSuggestionAdapterImpl().adapt(packageName, url)

        assertThat(result).isInstanceOf(SuggestionSource.WithUrl::class.java)
        assertThat((result as SuggestionSource.WithUrl).url).isEqualTo(url)
    }
}
