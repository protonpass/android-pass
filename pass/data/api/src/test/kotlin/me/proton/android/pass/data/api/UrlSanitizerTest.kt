package me.proton.android.pass.data.api

import me.proton.pass.common.api.Result
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UrlSanitizerTest {

    @Test
    fun `empty url should return error`() {
        val res = UrlSanitizer.sanitize("")
        assertTrue(res is Result.Error)
        assertNotNull(res.exception)
        assertTrue(res.exception is IllegalArgumentException)
    }

    @Test
    fun `url without scheme should have it added`() {
        val domain = "some.domain"
        val res = UrlSanitizer.sanitize(domain)
        assertTrue(res is Result.Success)
        assertEquals(res.data, "https://$domain")
    }

    @Test
    fun `url with scheme should return success and not have it edited`() {
        val domain = "ssh://some.domain"
        val res = UrlSanitizer.sanitize(domain)
        assertTrue(res is Result.Success)
        assertEquals(res.data, domain)
    }

    @Test
    fun `url with path has its path removed`() {
        val domain = "some.domain"
        val res = UrlSanitizer.sanitize("$domain/login")
        assertTrue(res is Result.Success)
        assertEquals(res.data, "https://$domain")
    }

    @Test
    fun `url with empty scheme should return error`() {
        val domain = "://some.domain"
        val res = UrlSanitizer.sanitize(domain)
        assertTrue(res is Result.Error)
    }

    @Test
    fun `url with spaces should return error`() {
        val domain = "https://url with spaces"
        val res = UrlSanitizer.sanitize(domain)
        assertTrue(res is Result.Error)
    }

    @Test
    fun `getDomain empty url should return error`() {
        val res = UrlSanitizer.getDomain("")
        assertTrue(res is Result.Error)
        assertNotNull(res.exception)
        assertTrue(res.exception is IllegalArgumentException)
    }

    @Test
    fun `getDomain should be able to extract domain from url with scheme`() {
        val domain = "a.b.c.d"
        val res = UrlSanitizer.getDomain("https://$domain/e?f=g")
        assertTrue(res is Result.Success)
        assertEquals(res.data, domain)
    }

    @Test
    fun `getDomain should be able to extract domain from url without scheme`() {
        val domain = "a.b.c.d"
        val res = UrlSanitizer.getDomain(domain)
        assertTrue(res is Result.Success)
        assertEquals(res.data, domain)
    }
}
