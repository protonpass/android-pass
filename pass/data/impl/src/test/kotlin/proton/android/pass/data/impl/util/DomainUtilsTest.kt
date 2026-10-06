/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.data.impl.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DomainUtilsTest {

    @Test
    fun `isDomainPartOf empty strings`() {
        val res = DomainUtils.isDomainPartOf("", "")
        assertThat(res).isFalse()
    }

    @Test
    fun `isDomainPartOf empty needle non-empty haystack`() {
        val res = DomainUtils.isDomainPartOf("", "example.test")
        assertThat(res).isFalse()
    }

    @Test
    fun `isDomainPartOf non-empty needle empty haystack`() {
        val res = DomainUtils.isDomainPartOf("example.test", "")
        assertThat(res).isFalse()
    }

    @Test
    fun `isDomainPartOf needle equal to haystack`() {
        val res = DomainUtils.isDomainPartOf("example.test", "example.test")
        assertThat(res).isTrue()
    }

    @Test
    fun `isDomainPartOf needle not equal to haystack`() {
        val res = DomainUtils.isDomainPartOf("example.test", "another.test")
        assertThat(res).isFalse()
    }

    @Test
    fun `isDomainPartOf needle is subdomain of haystack`() {
        val res = DomainUtils.isDomainPartOf("some.example.test", "example.test")
        assertThat(res).isTrue()
    }

    @Test
    fun `isDomainPartOf needle has stored domain as prefix without label boundary`() {
        val res = DomainUtils.isDomainPartOf("xexample.test", "example.test")
        assertThat(res).isFalse()
    }

    @Test
    fun `isDomainPartOf needle extends stored domain as suffix text`() {
        val res = DomainUtils.isDomainPartOf("example.teste", "example.test")
        assertThat(res).isFalse()
    }

    @Test
    fun `isDomainPartOf needle embeds stored domain mid-label`() {
        val res = DomainUtils.isDomainPartOf("evil-example.test.attacker.example", "example.test")
        assertThat(res).isFalse()
    }

    @Test
    fun `isDomainPartOf haystack contains needle`() {
        val res = DomainUtils.isDomainPartOf("example.test", "some.example.test")
        assertThat(res).isTrue()
    }

    @Test
    fun `isDomainPartOf haystack contains needle but is not it`() {
        val res = DomainUtils.isDomainPartOf("example.test", "dangerous.example.test.other.domain")
        assertThat(res).isFalse()
    }
}
