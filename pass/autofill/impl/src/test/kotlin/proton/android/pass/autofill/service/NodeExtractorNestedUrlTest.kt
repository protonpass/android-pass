/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.autofill.service

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.TestAutofillId
import proton.android.pass.autofill.TestUtils.parseResourceFile
import proton.android.pass.autofill.TestUtils.toAutofillNode
import proton.android.pass.autofill.heuristics.NodeExtractor

class NodeExtractorNestedUrlTest {

    @Suppress("MagicNumber", "UnderscoresInNumericLiterals")
    @Test
    fun `can extract the right url for nested fields`() {
        val parsed = parseResourceFile("other/app_autofillsample_autospill_web_focus.json")
        val asAutofillNode = parsed.rootContent.toAutofillNode()

        val res = NodeExtractor().extract(asAutofillNode)
        assertThat(res.fields.size).isEqualTo(4)

        assertThat(res.fields[0].id).isEqualTo(TestAutofillId(306156366))
        assertThat(res.fields[1].id).isEqualTo(TestAutofillId(306186157))
        assertThat(res.fields[2].id).isEqualTo(TestAutofillId(432415429))
        assertThat(res.fields[3].id).isEqualTo(TestAutofillId(432416390))

        assertThat(res.fields[0].url).isNull()
        assertThat(res.fields[1].url).isNull()
        assertThat(res.fields[2].url).isEqualTo("https://www.autofilth.lol")
        assertThat(res.fields[3].url).isEqualTo("https://www.autofilth.lol")
    }

    @Test
    fun `can extract the web url for chrome websites`() {
        val domain = "https://account.dyn.com"
        val parsed = parseResourceFile("login/chrome_account.dyn.com.json")
        val asAutofillNode = parsed.rootContent.toAutofillNode()

        val res = NodeExtractor().extract(asAutofillNode)
        assertThat(res.fields.all { it.url == domain }).isTrue()
    }

    @Test
    fun `can extract the web url for firefox websites`() {
        val domain = "https://news.ycombinator.com"
        val parsed = parseResourceFile("login/firefox_news.ycombinator.com_firstpassword.json")
        val asAutofillNode = parsed.rootContent.toAutofillNode()

        val res = NodeExtractor().extract(asAutofillNode)
        assertThat(res.fields.all { it.url == domain }).isTrue()
    }

    @Test
    fun `can extract the web url for duckduckgo websites`() {
        val domain = "https://account.proton.me"
        val parsed = parseResourceFile("login/duckduckgo.proton.me.username.json")
        val asAutofillNode = parsed.rootContent.toAutofillNode()

        val res = NodeExtractor().extract(asAutofillNode)
        assertThat(res.fields.all { it.url == domain }).isTrue()
    }

}
