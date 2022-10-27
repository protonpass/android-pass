package me.proton.android.pass.presentation

import me.proton.pass.common.api.Result
import me.proton.pass.presentation.UrlSanitizer
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UrlSanitizerTest {

    @Test
    fun `empty url should return success`() {
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
}
