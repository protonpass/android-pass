package me.proton.pass.presentation.create.alias

import org.junit.Test
import kotlin.test.assertEquals

class AliasUtilsTest {

    @Test
    fun `alias with empty title returns empty string`() {
        assertEquals(AliasUtils.formatAlias(""), "")
    }

    @Test
    fun `alias removes special characters`() {
        assertEquals(AliasUtils.formatAlias("a_b@c#d=e%f-g"), "a_bcdef-g")
    }

    @Test
    fun `alias removes spaces characters`() {
        assertEquals(AliasUtils.formatAlias("a b  c   d e"), "a-b--c---d-e")
    }

    @Test
    fun `alias clears capitalization`() {
        assertEquals(AliasUtils.formatAlias("aBCdEFg"), "abcdefg")
    }
}
