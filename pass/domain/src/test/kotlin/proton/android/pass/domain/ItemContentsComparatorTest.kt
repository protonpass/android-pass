/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ItemContentsComparatorTest {

    @Test
    fun `login with same autofill urls is equal`() {
        val urls = listOf(AutofillUrl(url = "https://*.example.com", mode = AutofillUrlMode.Pattern))

        val result = areItemContentsEqual(loginContents(urls), loginContents(urls)) { it }

        assertThat(result).isTrue()
    }

    @Test
    fun `login with removed advanced autofill url is not equal`() {
        val original = loginContents(
            listOf(AutofillUrl(url = "https://*.example.com", mode = AutofillUrlMode.Pattern))
        )
        val updated = loginContents(emptyList())

        val result = areItemContentsEqual(original, updated) { it }

        assertThat(result).isFalse()
    }

    @Test
    fun `login with changed advanced autofill url is not equal`() {
        val original = loginContents(
            listOf(AutofillUrl(url = "https://*.example.com", mode = AutofillUrlMode.Pattern))
        )
        val updated = loginContents(
            listOf(AutofillUrl(url = "https://*.other.com", mode = AutofillUrlMode.Pattern))
        )

        val result = areItemContentsEqual(original, updated) { it }

        assertThat(result).isFalse()
    }

    @Test
    fun `login with changed autofill url mode is not equal`() {
        val original = loginContents(
            listOf(AutofillUrl(url = "https://example.com", mode = AutofillUrlMode.Exact))
        )
        val updated = loginContents(
            listOf(AutofillUrl(url = "https://example.com", mode = AutofillUrlMode.StartWith))
        )

        val result = areItemContentsEqual(original, updated) { it }

        assertThat(result).isFalse()
    }

    private fun loginContents(autofillUrls: List<AutofillUrl>) = ItemContents.Login(
        title = "title",
        note = "",
        itemEmail = "user@example.com",
        itemUsername = "user",
        password = HiddenState.Empty(""),
        urls = emptyList(),
        packageInfoSet = emptySet(),
        primaryTotp = HiddenState.Empty(""),
        customFields = emptyList(),
        passkeys = emptyList(),
        autofillUrls = autofillUrls
    )
}
