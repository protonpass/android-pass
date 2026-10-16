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

package proton.android.pass.data.impl.url

import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.fakes.usecases.FakeGetPublicSuffixList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class HostParserImplTest {

    private lateinit var publicSuffixList: FakeGetPublicSuffixList
    private lateinit var instance: HostParserImpl

    @Before
    fun setup() {
        publicSuffixList = FakeGetPublicSuffixList()
        instance = HostParserImpl(publicSuffixList)
    }

    @Test
    fun `empty string should return error`() {
        publicSuffixList.setTlds(emptySet())
        val res = instance.parse("")

        assertTrue(res.isFailure)
    }

    @Test
    fun `symbols string should error`() {
        publicSuffixList.setTlds(emptySet())
        val res = instance.parse(".$%")

        assertTrue(res.isFailure)
    }

    @Test
    fun `can only contain allowed symbols`() {
        publicSuffixList.setTlds(emptySet())
        val res = instance.parse("a!b")

        assertTrue(res.isFailure)
    }

    @Test
    fun `is able to detect ipv4`() {
        val ip = "127.0.0.1"
        publicSuffixList.setTlds(emptySet())
        val res = instance.parse(ip)

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Ip)
        assertEquals(ip, hostInfo.ip)
    }

    @Test
    fun `wrong ipv4 is not detected as ip`() {
        publicSuffixList.setTlds(emptySet())
        val res = instance.parse("300.400.500.1")

        assertTrue(res.isFailure)
    }

    @Test
    fun `is able to detect tld correctly`() {
        val domain = "host"
        val tld = "com"
        publicSuffixList.setTlds(setOf(tld))
        val res = instance.parse("$domain.$tld")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertTrue(hostInfo.subdomain is None)
        assertEquals(domain, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `is able to detect the widest tld and the subdomain`() {
        val subdomain = "a.b.c.d"
        val domain = "host"
        val tld = "co.uk"
        publicSuffixList.setTlds(setOf("uk", "co", tld))
        val res = instance.parse("$subdomain.$domain.$tld")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(subdomain.some(), hostInfo.subdomain)
        assertEquals(domain, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `is able to detect no tld`() {
        val domain = "localhost"
        publicSuffixList.setTlds(setOf("com"))
        val res = instance.parse("localhost")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertTrue(hostInfo.subdomain is None)
        assertEquals(domain, hostInfo.domain)
        assertTrue(hostInfo.tld is None)
    }

    @Test
    fun `is able to detect tlds not in the list if there are no matches`() {
        val domain = "localhost"
        val tld = "somerandomtldthatdefinitelydoesnotexist"
        publicSuffixList.setTlds(setOf("com"))
        val res = instance.parse("$domain.$tld")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertTrue(hostInfo.subdomain is None)
        assertEquals(domain, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `is able to detect tlds not in the list if there are no matches together with subdomains`() {
        val subdomain = "a.b"
        val domain = "domain"
        val tld = "somerandomtldthatdefinitelydoesnotexist"
        publicSuffixList.setTlds(setOf("com"))
        val res = instance.parse("$subdomain.$domain.$tld")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(subdomain.some(), hostInfo.subdomain)
        assertEquals(domain, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `only tld is an error`() {
        val tld = "com"
        publicSuffixList.setTlds(setOf(tld))
        val res = instance.parse(tld)

        assertFalse(res.isSuccess)
    }

    @Test
    fun `is able to handle FQDN`() {
        val tld = "com"
        val domain = "domain"
        val subdomain = "subdomain"
        publicSuffixList.setTlds(setOf(tld))
        val res = instance.parse("$subdomain.$domain.$tld.") // Has a trailing dot

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(subdomain.some(), hostInfo.subdomain)
        assertEquals(domain, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `is able to parse domain that has the same subdomain as TLD`() {
        val tld = "com"
        val domain = "domain"
        publicSuffixList.setTlds(setOf(tld))
        val res = instance.parse("$tld.$domain.$tld")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(tld.some(), hostInfo.subdomain)
        assertEquals(domain, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `is able to parse domain that is the same as a TLD`() {
        val tld = "com"
        publicSuffixList.setTlds(setOf(tld))
        val res = instance.parse("$tld.$tld")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(None, hostInfo.subdomain)
        assertEquals(tld, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `is able to parse domain that is the same subdomain and domain as a TLD`() {
        val tld = "com"
        publicSuffixList.setTlds(setOf(tld))
        val res = instance.parse("$tld.$tld.$tld")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(tld.some(), hostInfo.subdomain)
        assertEquals(tld, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `is able to parse domain that has the TLD repeated in subdomains, domain and TLD`() {
        val tld = "com"
        val subdomain = "$tld.$tld.$tld"
        publicSuffixList.setTlds(setOf(tld))
        val res = instance.parse("$subdomain.$tld.$tld")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(subdomain.some(), hostInfo.subdomain)
        assertEquals(tld, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `is able to parse domain that has the TLD as the start and does not end with known TLD`() {
        val tld = "com"
        val domain = "domain"
        publicSuffixList.setTlds(setOf(tld))
        val res = instance.parse("$tld.$domain.unknown")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(tld.some(), hostInfo.subdomain)
        assertEquals(domain, hostInfo.domain)
        assertEquals("unknown".some(), hostInfo.tld)
    }

    @Test
    fun `can parse domain that has the TLD as the start and does not end with known TLD that has multiple parts`() {
        val tld = "long.tld"
        val domain = "domain"
        publicSuffixList.setTlds(setOf(tld))
        val res = instance.parse("$tld.$domain.unknown")

        assertTrue(res.isSuccess)

        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(tld.some(), hostInfo.subdomain)
        assertEquals(domain, hostInfo.domain)
        assertEquals("unknown".some(), hostInfo.tld)
    }

    @Test
    fun `input that only consists on the TLD is an error`() {
        val input = "the.tld"
        publicSuffixList.setTlds(setOf(input))

        val res = instance.parse(input)

        assertTrue(res.isFailure)
    }

    @Test
    fun `when public suffix list fails to load, returns Unparseable instead of guessing`() {
        publicSuffixList.setLoadFailure(IllegalStateException("psl unavailable"))
        val res = instance.parse("https://customer1.shared-hosting.example")

        assertTrue(res.isSuccess)
        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Unparseable)
        assertEquals("https", hostInfo.protocol)
        assertEquals("customer1.shared-hosting.example", hostInfo.rawHost)
    }

    @Test
    fun `when public suffix list fails to load, ip detection still works`() {
        publicSuffixList.setLoadFailure(IllegalStateException("psl unavailable"))
        val ip = "127.0.0.1"
        val res = instance.parse(ip)

        assertTrue(res.isSuccess)
        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Ip)
        assertEquals(ip, hostInfo.ip)
    }

    @Test
    fun `when the shared-hosting suffix is known, different tenants parse to different domains`() {
        publicSuffixList.setTlds(setOf("shared-hosting.example"))

        val tenantA = instance.parse("https://customer1.shared-hosting.example").getOrNull()
        val tenantB = instance.parse("https://customer2.shared-hosting.example").getOrNull()

        assertNotNull(tenantA)
        assertNotNull(tenantB)
        assertTrue(tenantA is HostInfo.Host)
        assertTrue(tenantB is HostInfo.Host)
        assertEquals("customer1", tenantA.domain)
        assertEquals("customer2", tenantB.domain)
        assertEquals("shared-hosting.example".some(), tenantA.tld)
        assertEquals("shared-hosting.example".some(), tenantB.tld)
    }

    @Test
    fun `regression- wildcard rule consumes one label as part of the tld, not flattened away`() {
        publicSuffixList.setWildcardBases(setOf("shared-hosting.example"))

        val tenantA = instance.parse("https://a.tenant1.shared-hosting.example").getOrNull()
        val tenantB = instance.parse("https://a.tenant2.shared-hosting.example").getOrNull()

        assertNotNull(tenantA)
        assertNotNull(tenantB)
        assertTrue(tenantA is HostInfo.Host)
        assertTrue(tenantB is HostInfo.Host)
        assertEquals("a", tenantA.domain)
        assertEquals("a", tenantB.domain)
        assertEquals("tenant1.shared-hosting.example".some(), tenantA.tld)
        assertEquals("tenant2.shared-hosting.example".some(), tenantB.tld)
    }

    @Test
    fun `exception rule overrides the wildcard it exempts`() {
        publicSuffixList.setWildcardBases(setOf("shared-hosting.example"))
        publicSuffixList.setExceptions(setOf("plain.shared-hosting.example"))

        val res = instance.parse("https://plain.shared-hosting.example")

        assertTrue(res.isSuccess)
        val hostInfo = res.getOrNull()
        assertNotNull(hostInfo)
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals("plain", hostInfo.domain)
        assertEquals("shared-hosting.example".some(), hostInfo.tld)
    }

    @Test
    fun `protocol and host are lowercased`() {
        publicSuffixList.setTlds(setOf("me"))

        val res = instance.parse("HTTPS://Account.Proton.ME/Login")

        assertEquals(
            HostInfo.Host(
                protocol = "https",
                subdomain = "account".some(),
                domain = "proton",
                tld = "me".some()
            ),
            res.getOrNull()
        )
    }

    @Test
    fun `punycode host is matched against the ascii form of an IDN public suffix`() {
        publicSuffixList.setTlds(setOf("no", "ålesund.no", "xn--lesund-hua.no"))

        val res = instance.parse("https://shop.xn--lesund-hua.no/login")

        assertEquals(
            HostInfo.Host(
                protocol = "https",
                subdomain = None,
                domain = "shop",
                tld = "xn--lesund-hua.no".some()
            ),
            res.getOrNull()
        )
    }

    @Test
    fun `hosts under different registrants of the same IDN public suffix get different domains`() {
        publicSuffixList.setTlds(setOf("no", "ålesund.no", "xn--lesund-hua.no"))

        val first = instance.parse("https://bank.xn--lesund-hua.no").getOrThrow()
        val second = instance.parse("https://other.xn--lesund-hua.no").getOrThrow()

        assertTrue(first is HostInfo.Host && second is HostInfo.Host)
        assertEquals("xn--lesund-hua.no".some(), (first as HostInfo.Host).tld)
        assertEquals("xn--lesund-hua.no".some(), (second as HostInfo.Host).tld)
        assertEquals("bank", first.domain)
        assertEquals("other", second.domain)
        assertNotEquals(first.domain, second.domain)
    }

    @Test
    fun `IdnNormalizer converts unicode rules and hosts to the same punycode form`() {
        assertEquals("xn--gmqw5a.hk", IdnNormalizer.toAscii("個人.hk"))
        assertEquals("xn--gmqw5a.hk", IdnNormalizer.toAscii("xn--gmqw5a.hk"))
        assertEquals("co.uk", IdnNormalizer.toAscii("CO.UK"))
    }
}
