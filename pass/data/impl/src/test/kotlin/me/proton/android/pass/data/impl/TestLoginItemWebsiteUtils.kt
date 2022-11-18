package me.proton.android.pass.data.impl

import me.proton.android.pass.data.impl.extensions.hasWebsite
import me.proton.pass.test.domain.TestItemType
import org.junit.Test
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
