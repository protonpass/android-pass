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

package proton.android.pass.features.itemcreate.login

import org.junit.Test
import proton.android.pass.data.api.usecases.popularservices.PopularService
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PopularServiceMatcherTest {

    private val services = listOf(
        service("Facebook"),
        service("Fast.com"),
        service("Google"),
        service("GitHub")
    )

    @Test
    fun `blank query returns nothing`() {
        assertTrue(matchPopularServices("", services).isEmpty())
        assertTrue(matchPopularServices("   ", services).isEmpty())
    }

    @Test
    fun `matches by case-insensitive title prefix`() {
        val result = matchPopularServices("fa", services).map { it.title }
        assertEquals(listOf("Facebook", "Fast.com"), result)
    }

    @Test
    fun `does not match a substring in the middle of the title (prefix only)`() {
        assertTrue(matchPopularServices("book", services).isEmpty())
    }

    @Test
    fun `query is trimmed before matching`() {
        val result = matchPopularServices("  GoO ", services).map { it.title }
        assertEquals(listOf("Google"), result)
    }

    @Test
    fun `returns the exact match so a fully typed title still shows as a suggestion`() {
        assertEquals(listOf("Facebook"), matchPopularServices("Facebook", services).map { it.title })
        assertEquals(listOf("Facebook"), matchPopularServices("facebook", services).map { it.title })
    }

    @Test
    fun `caps the number of results to the limit`() {
        val many = (1..10).map { service("Service$it") }
        assertEquals(3, matchPopularServices("Service", many, limit = 3).size)
    }

    private fun service(title: String) = PopularService(
        title = title,
        urls = listOf("https://${title.lowercase()}.example.com")
    )
}
