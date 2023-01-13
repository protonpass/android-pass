package proton.android.pass.data.impl.url

import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.some
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.fakes.usecases.TestGetPublicSuffixList
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HostParserImplTest {

    @Test
    fun `empty string should return error`() {
        val instance = HostParserImpl(TestGetPublicSuffixList(emptySet()))
        val res = instance.parse("")

        assertTrue(res is Result.Error)
    }

    @Test
    fun `is able to detect ipv4`() {
        val instance = HostParserImpl(TestGetPublicSuffixList(emptySet()))
        val res = instance.parse("127.0.0.1")

        assertTrue(res is Result.Success)

        val hostInfo = res.data
        assertTrue(hostInfo is HostInfo.Ip)
    }

    @Test
    fun `wrong ipv4 is not detected as ip`() {
        val instance = HostParserImpl(TestGetPublicSuffixList(emptySet()))
        val res = instance.parse("300.400.500.1")

        assertTrue(res is Result.Success)

        val hostInfo = res.data
        assertFalse(hostInfo is HostInfo.Ip)
    }

    @Test
    fun `is able to detect tld correctly`() {
        val domain = "host"
        val tld = "com"
        val instance = HostParserImpl(TestGetPublicSuffixList(setOf(tld)))
        val res = instance.parse("$domain.$tld")

        assertTrue(res is Result.Success)

        val hostInfo = res.data
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
        val instance = HostParserImpl(TestGetPublicSuffixList(setOf("uk", "co", tld)))
        val res = instance.parse("$subdomain.$domain.$tld")

        assertTrue(res is Result.Success)

        val hostInfo = res.data
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(subdomain.some(), hostInfo.subdomain)
        assertEquals(domain, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `is able to detect no tld`() {
        val domain = "localhost"
        val instance = HostParserImpl(TestGetPublicSuffixList(setOf("com")))
        val res = instance.parse("localhost")

        assertTrue(res is Result.Success)

        val hostInfo = res.data
        assertTrue(hostInfo is HostInfo.Host)
        assertTrue(hostInfo.subdomain is None)
        assertEquals(domain, hostInfo.domain)
        assertTrue(hostInfo.tld is None)
    }

    @Test
    fun `is able to detect tlds not in the list if there are no matches`() {
        val domain = "localhost"
        val tld = "somerandomtldthatdefinitelydoesnotexist"
        val instance = HostParserImpl(TestGetPublicSuffixList(setOf("com")))
        val res = instance.parse("$domain.$tld")

        assertTrue(res is Result.Success)

        val hostInfo = res.data
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
        val instance = HostParserImpl(TestGetPublicSuffixList(setOf("com")))
        val res = instance.parse("$subdomain.$domain.$tld")

        assertTrue(res is Result.Success)

        val hostInfo = res.data
        assertTrue(hostInfo is HostInfo.Host)
        assertEquals(subdomain.some(), hostInfo.subdomain)
        assertEquals(domain, hostInfo.domain)
        assertEquals(tld.some(), hostInfo.tld)
    }

    @Test
    fun `only tld is an error`() {
        val tld = "com"
        val instance = HostParserImpl(TestGetPublicSuffixList(setOf(tld)))
        val res = instance.parse(tld)

        assertFalse(res is Result.Success)
    }
}
