/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemcreate.alias

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.test.assertEquals

class AliasUtilsTest {

    @Test
    fun `alias with vocals with accents in title returns vocals without accents`() {
        assertEquals(AliasUtils.formatAlias("áàäâãāéèëêėē"), "aaaaaaeeeeee")
    }

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
    fun `alias clears all first dots`() {
        assertEquals(AliasUtils.formatAlias(".....abcd.efg"), "abcd.efg")
    }

    @Test
    fun `alias removes all consecutive dots`() {
        assertEquals(AliasUtils.formatAlias("abcd.....efg"), "abcd.efg")
    }

    @Test
    fun `should be able to extract the prefix and suffix`() {
        val prefix = "some.random"
        val suffix = "suffix@domain.tld"
        val res = AliasUtils.extractPrefixSuffix("$prefix.$suffix")
        assertThat(res.prefix).isEqualTo(prefix)
        assertThat(res.suffix).isEqualTo(suffix)
    }

    @Test
    fun `can extract prefix and suffix from prefix without dot`() {
        val prefix = "some-random"
        val suffix = "@domain.tld"
        val res = AliasUtils.extractPrefixSuffix("${prefix}$suffix")
        assertThat(res.prefix).isEqualTo(prefix)
        assertThat(res.suffix).isEqualTo(suffix)
    }
}
