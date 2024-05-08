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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.url.HostParser
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import proton.android.pass.preferences.LastItemAutofillPreference
import javax.inject.Inject

interface SuggestionSorter {
    fun sort(
        items: List<Item>,
        url: Option<String>,
        lastItemAutofill: Option<LastItemAutofillPreference>
    ): List<Item>
}

class SuggestionSorterImpl @Inject constructor(
    private val hostParser: HostParser
) : SuggestionSorter {

    override fun sort(
        items: List<Item>,
        url: Option<String>,
        lastItemAutofill: Option<LastItemAutofillPreference>
    ): List<Item> = when (url) {
        is Some -> sortWithUrl(items, url.value)
        else -> items
    }.putLastItemAutofillOnTop(lastItemAutofill)

    private fun List<Item>.putLastItemAutofillOnTop(
        lastItemAutofillOption: Option<LastItemAutofillPreference>
    ): List<Item> = when (lastItemAutofillOption) {
        is Some -> {
            val lastItemAutofill = lastItemAutofillOption.value
            val lastItem = find { it.id.id == lastItemAutofill.itemId }
                ?.takeIf { !lastItemAutofill.isAutofillPreferenceTooOld(Clock.System.now().epochSeconds) }
            lastItem?.let {
                val mutableItems = toMutableList()
                mutableItems.remove(it)
                mutableItems.add(0, it)
                mutableItems
            } ?: this
        }
        else -> this
    }

    private fun sortWithUrl(items: List<Item>, url: String): List<Item> {
        val parsed = hostParser.parse(url).fold(
            onSuccess = { it },
            onFailure = { return items }
        )

        return when (parsed) {
            is HostInfo.Ip -> items // If it's an IP, there's no sorting that we can perform
            is HostInfo.Host -> sortWithDomainInfo(parsed, items)
        }
    }

    private fun sortWithDomainInfo(parsed: HostInfo.Host, items: List<Item>): List<Item> {
        val loginItems = getLoginItems(items)
        return when (parsed.subdomain) {
            None -> sortByDomain(parsed, loginItems)
            is Some -> sortBySubdomain(parsed, loginItems)
        }
    }

    /**
     * Logic:
     * If user is on sub.domain.com:
     *   show sub.domain.com items on top,
     *   then domain.com items,
     *   and lastly other.domain.com items
     */
    private fun sortBySubdomain(parsed: HostInfo.Host, items: List<LoginItem>): List<Item> {
        val sameSubdomainItems = mutableListOf<Item>()
        val domainItems = mutableListOf<Item>()
        val otherSubdomainItems = mutableListOf<Item>()

        items.forEach { loginItem ->
            val parsedWebsites = loginItem.login.websites
                .map { url -> hostParser.parse(url) }
                .filter { it.isSuccess }
                .mapNotNull { it.getOrNull() }
                .filterIsInstance<HostInfo.Host>()

            val shouldGoToSameSubdomain = parsedWebsites.any {
                it.subdomain == parsed.subdomain && it.domain == parsed.domain
            }
            if (shouldGoToSameSubdomain) {
                sameSubdomainItems.add(loginItem.item)
            } else {
                val shouldGoToDomainItems = parsedWebsites.any {
                    it.subdomain is None && it.domain == parsed.domain
                }
                if (shouldGoToDomainItems) {
                    domainItems.add(loginItem.item)
                } else {
                    otherSubdomainItems.add(loginItem.item)
                }
            }
        }

        val finalList = mutableListOf<Item>()
        finalList.addAll(sameSubdomainItems)
        finalList.addAll(domainItems)
        finalList.addAll(otherSubdomainItems)
        return finalList
    }

    /**
     * Logic:
     * If user is on domain.com
     *   show domain.com items on top,
     *   then sub.domain.com items
     */
    private fun sortByDomain(parsed: HostInfo.Host, items: List<LoginItem>): List<Item> {
        val domainItems = mutableListOf<Item>()
        val subdomainItems = mutableListOf<Item>()

        items.forEach { loginItem ->
            val parsedWebsites = loginItem.login.websites
                .map { url -> hostParser.parse(url) }
                .filter { it.isSuccess }
                .mapNotNull { it.getOrNull() }
                .filterIsInstance<HostInfo.Host>()

            val shouldGoToDomain = parsedWebsites.any {
                it.domain == parsed.domain && it.subdomain is None
            }
            if (shouldGoToDomain) {
                domainItems.add(loginItem.item)
            } else {
                subdomainItems.add(loginItem.item)
            }
        }

        val finalList = mutableListOf<Item>()
        finalList.addAll(domainItems)
        finalList.addAll(subdomainItems)
        return finalList
    }

    private fun getLoginItems(items: List<Item>): List<LoginItem> {
        val res = mutableListOf<LoginItem>()
        for (item in items) {
            if (item.itemType is ItemType.Login) {
                res.add(LoginItem(item = item, login = item.itemType as ItemType.Login))
            }
        }
        return res
    }

    internal data class LoginItem(
        val item: Item,
        val login: ItemType.Login
    )
}
