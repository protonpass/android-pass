package proton.android.pass.data.api

import org.junit.Test
import proton.android.pass.data.api.url.UrlSanitizer
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UrlSanitizerTest {

    @Test
    fun `empty url should return error`() {
        val res = UrlSanitizer.sanitize("")
        assertTrue(res.isFailure)

        val exception = res.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun `url without scheme should have it added`() {
        val domain = "some.domain"
        val res = UrlSanitizer.sanitize(domain)
        assertTrue(res.isSuccess)
        assertEquals(res.getOrThrow(), "https://$domain")
    }

    @Test
    fun `symbols string should error`() {
        val domain = ".$%"
        val res = UrlSanitizer.sanitize(domain)
        assertTrue(res.isFailure)
    }

    @Test
    fun `only accepted symbols`() {
        val domain = "a!b"
        val res = UrlSanitizer.getDomain(domain)
        assertTrue(res.isFailure)
    }

    @Test
    fun `is able to handle IP addresses`() {
        val domain = "1.2.3.4"
        val res = UrlSanitizer.getDomain(domain)
        assertTrue(res.isSuccess)
        assertEquals(domain, res.getOrThrow())
    }

    @Test
    fun `url with scheme should return success and not have it edited`() {
        val domain = "ssh://some.domain"
        val res = UrlSanitizer.sanitize(domain)
        assertTrue(res.isSuccess)
        assertEquals(res.getOrThrow(), domain)
    }

    @Test
    fun `url with path preserves the path`() {
        val domain = "some.domain"
        val path = "login"
        val res = UrlSanitizer.sanitize("$domain/$path")
        assertTrue(res.isSuccess)
        assertEquals(res.getOrThrow(), "https://$domain/$path")
    }

    @Test
    fun `url with empty scheme should return error`() {
        val domain = "://some.domain"
        val res = UrlSanitizer.sanitize(domain)
        assertTrue(res.isFailure)
    }

    @Test
    fun `url with spaces should return error`() {
        val domain = "https://url with spaces"
        val res = UrlSanitizer.sanitize(domain)
        assertTrue(res.isFailure)
    }

    @Test
    fun `getDomain empty url should return error`() {
        val res = UrlSanitizer.getDomain("")
        assertTrue(res.isFailure)

        val exception = res.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun `getDomain should be able to extract domain from url with scheme`() {
        val domain = "a.b.c.d"
        val res = UrlSanitizer.getDomain("https://$domain/e?f=g")
        assertTrue(res.isSuccess)
        assertEquals(res.getOrThrow(), domain)
    }

    @Test
    fun `getDomain should be able to extract domain from url without scheme`() {
        val domain = "a.b.c.d"
        val res = UrlSanitizer.getDomain(domain)
        assertTrue(res.isSuccess)
        assertEquals(res.getOrThrow(), domain)
    }

    @Test
    fun `getDomain should remove the final dot of the host section`() {
        val cases = mapOf(
            "a.b.c.d." to "a.b.c.d",
            "https://domain.test." to "domain.test",
            "https://domain.test./" to "domain.test",
            "ftp://domain.test." to "domain.test"
        )

        cases.forEach { (input, expected) ->
            val res = UrlSanitizer.getDomain(input)
            assertEquals(expected, res.getOrThrow())
        }
    }

    @Test
    fun `getProtocol should be able to extract the correct scheme`() {
        val scheme = "someprotocol"
        val domain = "$scheme://a.b.c.d"
        val res = UrlSanitizer.getProtocol(domain)
        assertTrue(res.isSuccess)
        assertEquals(res.getOrThrow(), scheme)
    }

    @Test
    fun `getProtocol should return https if there is no scheme`() {
        val domain = "a.b.c.d"
        val res = UrlSanitizer.getProtocol(domain)
        assertTrue(res.isSuccess)
        assertEquals(res.getOrThrow(), "https")
    }

    @Test
    fun `sanitize does not allow urls to start with malicious schemes`() {
        val cases = listOf(
            "javascript:some.domain",
            "javascript:/some.domain",
            "javascript://some.domain",
            "javascript:///some.domain",
            "data:some.domain",
            "data:/some.domain",
            "data://some.domain",
            "data:///some.domain",
            "file:some.domain",
            "file:/some.domain",
            "file://some.domain",
            "file:///some.domain",
            "about:some.domain",
            "about:/some.domain",
            "about://some.domain",
            "about:///some.domain",
            "blob:some.domain",
            "blob:/some.domain",
            "blob://some.domain",
            "blob:///some.domain",
        )

        cases.forEach { input ->
            val res = UrlSanitizer.sanitize(input)
            assertTrue(res.isFailure)
        }
    }
}
