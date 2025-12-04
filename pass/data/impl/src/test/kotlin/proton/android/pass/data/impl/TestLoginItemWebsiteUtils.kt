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

package proton.android.pass.data.impl

import org.junit.Test
import proton.android.pass.data.impl.extensions.hasWebsite
import proton.android.pass.test.domain.TestItemType
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestLoginItemWebsiteUtils {

    @Test
    fun `hasWebsite with empty list`() {
        val item = TestItemType.login()
        assertFalse(item.hasWebsite("randomweb"))
    }

    @Test
    fun `hasWebsite with same host`() {
        val url = "random.local"
        val item = TestItemType.login(websites = listOf(url))
        assertTrue(item.hasWebsite(url))
    }

    @Test
    fun `hasWebsite with different port`() {
        val host = "random.local"
        val queryUrl = "$host:1234"
        val itemUrl = "$host:9876"
        val item = TestItemType.login(websites = listOf(itemUrl))
        assertFalse(item.hasWebsite(queryUrl))
    }

    @Test
    fun `hasWebsite with different path`() {
        val url = "random.local"
        val queryUrl = "$url/path"
        val itemUrl = "$url/other"
        val item = TestItemType.login(websites = listOf(itemUrl))
        assertTrue(item.hasWebsite(queryUrl))
    }

    @Test
    fun `hasWebsite with subdomain in param`() {
        val host = "random.local"
        val queryUrl = "query.$host"
        val itemUrl = "item.$host"
        val item = TestItemType.login(websites = listOf(itemUrl))
        assertFalse(item.hasWebsite(queryUrl))
    }
}
