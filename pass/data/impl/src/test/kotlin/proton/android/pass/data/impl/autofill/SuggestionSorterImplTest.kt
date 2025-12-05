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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.fakes.usecases.FakeGetPublicSuffixList
import proton.android.pass.data.impl.url.HostParserImpl
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.preferences.LastItemAutofillPreference
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ItemTypeTestFactory
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class SuggestionSorterImplTest {

    private lateinit var publicSuffixList: FakeGetPublicSuffixList
    private lateinit var instance: SuggestionSorterImpl

    @Before
    fun setup() {
        publicSuffixList = FakeGetPublicSuffixList()
        instance = SuggestionSorterImpl(HostParserImpl(publicSuffixList))
    }

    @Test
    fun `none url just returns the same list`() {
        val list = listOf(
            ItemTestFactory.random(ItemTypeTestFactory.login()),
            ItemTestFactory.random(ItemTypeTestFactory.login())
        ).map { ItemData.SuggestedItem(it, DEFAULT_SUGGESTION) }

        val res = instance.sort(items = list, lastItemAutofill = None)
        assertEquals(res, list)
    }

    @Test
    fun `invalid url just returns the same list`() {
        val list = listOf(
            ItemTestFactory.random(ItemTypeTestFactory.login()),
            ItemTestFactory.random(ItemTypeTestFactory.login())
        ).map { ItemData.SuggestedItem(it, Suggestion.Url("some invalid url")) }

        val res = instance.sort(items = list, lastItemAutofill = None)
        assertEquals(res, list)
    }

    @Test
    fun `using an ip returns the same list`() {
        val ip = "1.2.3.4"
        val list = listOf(
            ItemTestFactory.random(ItemTypeTestFactory.login(websites = listOf(ip))),
            ItemTestFactory.random(ItemTypeTestFactory.login(websites = listOf(ip)))
        ).map { ItemData.SuggestedItem(it, Suggestion.Url(ip)) }

        val res = instance.sort(items = list, lastItemAutofill = None)
        assertEquals(res, list)
    }

    @Test
    fun `sort with domain`() {
        val tld = "sometld"
        val domain = "somedomain.$tld"
        val item1 = ItemTestFactory.random(ItemTypeTestFactory.login(websites = listOf("subd.$domain")))
        val item2 = ItemTestFactory.random(ItemTypeTestFactory.login(websites = listOf("a.b.$domain")))
        val item3 = ItemTestFactory.random(ItemTypeTestFactory.login(websites = listOf(domain)))
        publicSuffixList.setTlds(setOf(tld))

        val items = listOf(item1, item2, item3)
            .map { ItemData.SuggestedItem(it, Suggestion.Url(domain)) }

        // The result should be [item3, item1, item2] as we want the domain match before the
        // subdomain when we filtered by domain
        val res = instance.sort(
            items = items,
            lastItemAutofill = None
        )

        val expected = listOf(item3, item1, item2)
            .map { ItemData.SuggestedItem(it, Suggestion.Url(domain)) }
        assertEquals(res, expected)
    }

    @Test
    fun `sort with subdomain`() {
        val tld = "sometld"
        val domain = "somedomain.$tld"
        val subdomain = "somesubdomain.$domain"
        val item1 = ItemTestFactory.random(ItemTypeTestFactory.login(websites = listOf("other.$domain")))
        val item2 = ItemTestFactory.random(ItemTypeTestFactory.login(websites = listOf("random.$domain")))
        val item3 = ItemTestFactory.random(ItemTypeTestFactory.login(websites = listOf(subdomain)))
        val item4 = ItemTestFactory.random(ItemTypeTestFactory.login(websites = listOf(domain)))
        publicSuffixList.setTlds(setOf(tld))


        // The result should be [item3, item4, item1, item2] as we want the order:
        // - exact subdomain match
        // - only base domain
        // - rest of subdomains
        val items = listOf(item1, item2, item3, item4)
            .map { ItemData.SuggestedItem(it, Suggestion.Url(subdomain)) }
        val res = instance.sort(
            items = items,
            lastItemAutofill = None
        )
        val expected = listOf(item3, item4, item1, item2)
            .map { ItemData.SuggestedItem(it, Suggestion.Url(subdomain)) }
        assertEquals(res, expected)
    }

    @Test
    fun `last item autofill - last item exists and not too old`() {
        val itemId = "lastItemId"
        val shareId = "lastShareId"
        val currentTime = Clock.System.now().epochSeconds
        val notTooOldTime = currentTime - 10
        val lastItemAutofillPreference = LastItemAutofillPreference(notTooOldTime, shareId, itemId)
        val item1 = ItemTestFactory.create(
            itemId = ItemId(itemId),
            shareId = ShareId(shareId),
            itemType = ItemTypeTestFactory.login()
        )
        val item2 = ItemTestFactory.random(ItemTypeTestFactory.login())

        val items = listOf(item2, item1)
            .map { ItemData.SuggestedItem(it, DEFAULT_SUGGESTION) }
        val res = instance.sort(
            items = items,
            lastItemAutofill = lastItemAutofillPreference.some()
        )

        val expected = listOf(item1, item2)
            .map { ItemData.SuggestedItem(it, DEFAULT_SUGGESTION) }
        assertEquals(res, expected)
    }

    @Test
    fun `last item autofill - last item exists but too old`() {
        val itemId = "lastItemId"
        val shareId = "lastShareId"
        val currentTime = Clock.System.now().epochSeconds
        val tooOldTime = currentTime - 2.minutes.inWholeSeconds
        val lastItemAutofillPreference = LastItemAutofillPreference(tooOldTime, shareId, itemId)
        val item1 = ItemTestFactory.create(
            itemId = ItemId(itemId),
            shareId = ShareId(shareId),
            itemType = ItemTypeTestFactory.login()
        )
        val item2 = ItemTestFactory.create(itemType = ItemTypeTestFactory.login())

        val items = listOf(item2, item1)
            .map { ItemData.SuggestedItem(it, DEFAULT_SUGGESTION) }
        val res = instance.sort(
            items = items,
            lastItemAutofill = lastItemAutofillPreference.some()
        )

        val expected = listOf(item2, item1)
            .map { ItemData.SuggestedItem(it, DEFAULT_SUGGESTION) }
        assertEquals(res, expected)
    }

    @Test
    fun `sort credit cards`() {
        val currentTime = Clock.System.now()
        val card1 = ItemTestFactory.random(
            itemType = ItemTypeTestFactory.creditCard(),
            lastAutofillTime = currentTime.toEpochMilliseconds()
        )
        val card2 = ItemTestFactory.random(
            itemType = ItemTypeTestFactory.creditCard(),
            lastAutofillTime = currentTime.minus(60, DateTimeUnit.SECOND).toEpochMilliseconds()
        )
        val card3 = ItemTestFactory.random(
            itemType = ItemTypeTestFactory.creditCard(),
            modificationTime = currentTime.minus(120, DateTimeUnit.SECOND).toEpochMilliseconds()
        )
        val card4 = ItemTestFactory.random(
            itemType = ItemTypeTestFactory.creditCard(),
            modificationTime = currentTime.minus(180, DateTimeUnit.SECOND).toEpochMilliseconds()
        )

        val items = listOf(card4, card3, card2, card1)
            .map { ItemData.SuggestedItem(it, DEFAULT_SUGGESTION) }

        val res = instance.sort(items, None)

        val expected: List<ItemData.SuggestedItem> = listOf(card1, card2, card3, card4)
            .map { ItemData.SuggestedItem(it, DEFAULT_SUGGESTION) }

        // The result should be sorted by last autofill time descending, then by modification time descending
        assertEquals(res, expected)
    }

    @Test
    fun `sort logins by package names, regular urls, and dal suggestions`() {
        val tld = "sometld"
        val domain = "somedomain.$tld"
        val packageNameSuggestion = Suggestion.PackageName("com.example")
        val regularUrlSuggestion = Suggestion.Url("https://$domain")
        val dalSuggestion = Suggestion.Url("https://dal.$domain", isDALSuggestion = true)

        val item1 = ItemData.SuggestedItem(ItemTestFactory.random(ItemTypeTestFactory.login()), packageNameSuggestion)
        val item2 = ItemData.SuggestedItem(ItemTestFactory.random(ItemTypeTestFactory.login()), regularUrlSuggestion)
        val item3 = ItemData.SuggestedItem(ItemTestFactory.random(ItemTypeTestFactory.login()), dalSuggestion)

        val items = listOf(item3, item1, item2)

        val res = instance.sort(items, None)

        val expected = listOf(item1, item2, item3)

        assertEquals(res.map { it.item.id }, expected.map { it.item.id })
    }

    companion object {
        private val DEFAULT_SUGGESTION = Suggestion.PackageName("com.example")
    }
}
