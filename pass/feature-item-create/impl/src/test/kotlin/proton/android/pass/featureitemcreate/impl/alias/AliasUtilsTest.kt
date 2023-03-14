package proton.android.pass.featureitemcreate.impl.alias

import com.google.common.truth.Truth.assertThat
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

    @Test
    fun `should be able to extract the prefix and suffix`() {
        val prefix = "some.random"
        val suffix = "suffix@domain.tld"
        val res = AliasUtils.extractPrefixSuffix("$prefix.$suffix")
        assertThat(res.prefix).isEqualTo(prefix)
        assertThat(res.suffix).isEqualTo(suffix)
    }
}
