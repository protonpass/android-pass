/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.data.api.autosave

import org.junit.Test
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import kotlin.test.assertEquals

class AutosaveLoginMatcherTest {

    private fun loginWith(websites: List<String> = emptyList(), packageInfoSet: Set<PackageInfo> = emptySet()) =
        ItemType.Login(
            itemEmail = "user@test.com",
            itemUsername = "user",
            password = "",
            websites = websites,
            packageInfoSet = packageInfoSet,
            primaryTotp = "",
            customFields = emptyList(),
            passkeys = emptyList()
        )

    // --- Website matching ---

    @Test
    fun `exact same url matches`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", null)
        val login = loginWith(websites = listOf("https://example.com"))
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `same domain different path does not match`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com/login", null)
        val login = loginWith(websites = listOf("https://example.com/settings"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `item without scheme matches url with scheme`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", null)
        val login = loginWith(websites = listOf("example.com"))
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `url with trailing slash matches without`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", null)
        val login = loginWith(websites = listOf("https://example.com/"))
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `url with multiple trailing slash matches without`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", null)
        val login = loginWith(websites = listOf("https://example.com////"))
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `different domains do not match`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", null)
        val login = loginWith(websites = listOf("https://other.com"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `subdomain does not match parent domain`() {
        val matcher = AutosaveLoginMatcher("user", "https://sub.example.com", null)
        val login = loginWith(websites = listOf("https://example.com"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `parent domain does not match subdomain`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", null)
        val login = loginWith(websites = listOf("https://sub.example.com"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `same domain same port different path does not match`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com:8080/page", null)
        val login = loginWith(websites = listOf("https://example.com:8080"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `same domain same port exact match`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com:8080", null)
        val login = loginWith(websites = listOf("https://example.com:8080"))
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `different port does not match`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com:8080", null)
        val login = loginWith(websites = listOf("https://example.com"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `empty websites list returns false`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", null)
        val login = loginWith(websites = emptyList())
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `invalid website in matcher returns false`() {
        val matcher = AutosaveLoginMatcher("user", "://invalid", null)
        val login = loginWith(websites = listOf("https://example.com"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `matches among multiple websites`() {
        val matcher = AutosaveLoginMatcher("user", "https://target.com", null)
        val login = loginWith(websites = listOf("https://other.com", "https://target.com", "https://another.com"))
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `ip address exact match`() {
        val matcher = AutosaveLoginMatcher("user", "https://192.168.1.1", null)
        val login = loginWith(websites = listOf("https://192.168.1.1"))
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `ip address different path does not match`() {
        val matcher = AutosaveLoginMatcher("user", "https://192.168.1.1/admin", null)
        val login = loginWith(websites = listOf("https://192.168.1.1"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `different ip addresses do not match`() {
        val matcher = AutosaveLoginMatcher("user", "https://192.168.1.1", null)
        val login = loginWith(websites = listOf("https://192.168.1.2"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `different schemes do not match`() {
        val matcher = AutosaveLoginMatcher("user", "http://example.com", null)
        val login = loginWith(websites = listOf("https://example.com"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `url with query params does not match base url`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com/login?redirect=home", null)
        val login = loginWith(websites = listOf("https://example.com"))
        assertEquals(false, matcher.matchesSource(login))
    }

    @Test
    fun `trailing dot in domain matches without`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com.", null)
        val login = loginWith(websites = listOf("https://example.com"))
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `blank website in item is ignored`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", null)
        val login = loginWith(websites = listOf("", "   "))
        assertEquals(false, matcher.matchesSource(login))
    }

    // --- www subdomain matching ---

    @Test
    fun `website with www matches item without www`() {
        val matcher = AutosaveLoginMatcher("user", "https://www.example.com", null)
        val login = loginWith(websites = listOf("https://example.com"))
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `website without www matches item with www`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", null)
        val login = loginWith(websites = listOf("https://www.example.com"))
        assertEquals(true, matcher.matchesSource(login))
    }

    // --- Package name matching ---

    @Test
    fun `package name matches`() {
        val matcher = AutosaveLoginMatcher("user", null, "com.example.app")
        val login = loginWith(
            packageInfoSet = setOf(PackageInfo(PackageName("com.example.app"), AppName("Example")))
        )
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `different package name does not match`() {
        val matcher = AutosaveLoginMatcher("user", null, "com.example.app")
        val login = loginWith(
            packageInfoSet = setOf(PackageInfo(PackageName("com.other.app"), AppName("Other")))
        )
        assertEquals(false, matcher.matchesSource(login))
    }

    // --- No website No packageName matching ---

    @Test
    fun `no website and no package name returns null`() {
        val matcher = AutosaveLoginMatcher("user", null, null)
        val login = loginWith(websites = listOf("https://example.com"))
        assertEquals(false, matcher.matchesSource(login))
    }

    // --- website and packageName matching ---

    @Test
    fun `package name takes priority over website and packageName match`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", "com.example.app")
        val login = loginWith(
            websites = listOf("https://other.com"),
            packageInfoSet = setOf(PackageInfo(PackageName("com.example.app"), AppName("Example")))
        )
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `package name takes priority over website but login packageName not found and website match`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", "com.example.app")
        val login = loginWith(
            websites = listOf("https://example.com"),
            packageInfoSet = setOf(PackageInfo(PackageName("com.another.app"), AppName("another")))
        )
        assertEquals(true, matcher.matchesSource(login))
    }

    @Test
    fun `package name takes priority over website but login packageName and website do not match`() {
        val matcher = AutosaveLoginMatcher("user", "https://example.com", "com.example.app")
        val login = loginWith(
            websites = listOf("https://another.com"),
            packageInfoSet = setOf(PackageInfo(PackageName("com.another.app"), AppName("another")))
        )
        assertEquals(false, matcher.matchesSource(login))
    }
}
