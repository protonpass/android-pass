package me.proton.android.pass.presentation.utils

import com.google.common.truth.Truth.assertThat
import me.proton.pass.presentation.utils.AliasUtils
import org.junit.Test

class AliasUtilsTest {

    @Test
    fun `should be able to extract the prefix and suffix`() {
        val prefix = "some.random"
        val suffix = "suffix@domain.tld"
        val res = AliasUtils.extractPrefixSuffix("$prefix.$suffix")
        assertThat(res.prefix).isEqualTo(prefix)
        assertThat(res.suffix).isEqualTo(suffix)
    }
}
