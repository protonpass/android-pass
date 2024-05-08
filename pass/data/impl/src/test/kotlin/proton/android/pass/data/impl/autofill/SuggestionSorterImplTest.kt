/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.impl.autofill

import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.data.fakes.usecases.TestGetPublicSuffixList
import proton.android.pass.data.impl.url.HostParserImpl
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.preferences.LastItemAutofillPreference
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestItemType
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class SuggestionSorterImplTest {

    private lateinit var publicSuffixList: TestGetPublicSuffixList
    private lateinit var instance: SuggestionSorterImpl

    @Before
    fun setup() {
        publicSuffixList = TestGetPublicSuffixList()
        instance = SuggestionSorterImpl(HostParserImpl(publicSuffixList))
    }

    @Test
    fun `none url just returns the same list`() {
        val list = listOf(
            TestItem.random(TestItemType.login()),
            TestItem.random(TestItemType.login())
        )

        val res = instance.sort(
            items = list,
            url = None,
            lastItemAutofill = None
        )
        assertEquals(res, list)
    }

    @Test
    fun `invalid url just returns the same list`() {
        val list = listOf(
            TestItem.random(TestItemType.login()),
            TestItem.random(TestItemType.login())
        )

        val res = instance.sort(
            items = list,
            url = "some invalid url".some(),
            lastItemAutofill = None
        )
        assertEquals(res, list)
    }

    @Test
    fun `using an ip returns the same list`() {
        val ip = "1.2.3.4"
        val list = listOf(
            TestItem.random(TestItemType.login(websites = listOf(ip))),
            TestItem.random(TestItemType.login(websites = listOf(ip)))
        )

        val res = instance.sort(
            items = list,
            url = ip.some(),
            lastItemAutofill = None
        )
        assertEquals(res, list)
    }

    @Test
    fun `sort with domain`() {
        val tld = "sometld"
        val domain = "somedomain.$tld"
        val item1 = TestItem.random(TestItemType.login(websites = listOf("subd.$domain")))
        val item2 = TestItem.random(TestItemType.login(websites = listOf("a.b.$domain")))
        val item3 = TestItem.random(TestItemType.login(websites = listOf(domain)))
        publicSuffixList.setTlds(setOf(tld))


        // The result should be [item3, item1, item2] as we want the domain match before the
        // subdomain when we filtered by domain
        val res = instance.sort(
            items = listOf(item1, item2, item3),
            url = domain.some(),
            lastItemAutofill = None
        )
        assertEquals(res, listOf(item3, item1, item2))
    }

    @Test
    fun `sort with subdomain`() {
        val tld = "sometld"
        val domain = "somedomain.$tld"
        val subdomain = "somesubdomain.$domain"
        val item1 = TestItem.random(TestItemType.login(websites = listOf("other.$domain")))
        val item2 = TestItem.random(TestItemType.login(websites = listOf("random.$domain")))
        val item3 = TestItem.random(TestItemType.login(websites = listOf(subdomain)))
        val item4 = TestItem.random(TestItemType.login(websites = listOf(domain)))
        publicSuffixList.setTlds(setOf(tld))


        // The result should be [item3, item4, item1, item2] as we want the order:
        // - exact subdomain match
        // - only base domain
        // - rest of subdomains
        val items = listOf(item1, item2, item3, item4)
        val res = instance.sort(
            items = items,
            url = subdomain.some(),
            lastItemAutofill = None
        )
        assertEquals(res, listOf(item3, item4, item1, item2))
    }

    @Test
    fun `last item autofill - last item exists and not too old`() {
        val itemId = "lastItemId"
        val shareId = "lastShareId"
        val currentTime = Clock.System.now().epochSeconds
        val notTooOldTime = currentTime - 10
        val lastItemAutofillPreference = LastItemAutofillPreference(notTooOldTime, shareId, itemId)
        val item1 = TestItem.create(
            itemId = ItemId(itemId),
            shareId = ShareId(shareId),
            itemType = TestItemType.login()
        )
        val item2 = TestItem.random(TestItemType.login())

        val res = instance.sort(
            items = listOf(item2, item1),
            url = None,
            lastItemAutofill = lastItemAutofillPreference.some()
        )

        assertEquals(res, listOf(item1, item2))
    }

    @Test
    fun `last item autofill - last item exists but too old`() {
        val itemId = "lastItemId"
        val shareId = "lastShareId"
        val currentTime = Clock.System.now().epochSeconds
        val tooOldTime = currentTime - 2.minutes.inWholeSeconds
        val lastItemAutofillPreference = LastItemAutofillPreference(tooOldTime, shareId, itemId)
        val item1 = TestItem.create(
            itemId = ItemId(itemId),
            shareId = ShareId(shareId),
            itemType = TestItemType.login()
        )
        val item2 = TestItem.create(itemType = TestItemType.login())

        val res = instance.sort(
            items = listOf(item2, item1),
            url = None,
            lastItemAutofill = lastItemAutofillPreference.some()
        )

        assertEquals(res, listOf(item2, item1))
    }
}
