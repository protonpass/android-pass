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

package proton.android.pass.data.impl.autofill

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.url.HostParser
import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.domain.ItemType
import proton.android.pass.preferences.LastItemAutofillPreference
import javax.inject.Inject

interface SuggestionSorter {
    fun sort(
        items: List<ItemData.SuggestedItem>,
        lastItemAutofill: Option<LastItemAutofillPreference>
    ): List<ItemData.SuggestedItem>
}

class SuggestionSorterImpl @Inject constructor(
    private val hostParser: HostParser
) : SuggestionSorter {

    override fun sort(
        items: List<ItemData.SuggestedItem>,
        lastItemAutofill: Option<LastItemAutofillPreference>
    ): List<ItemData.SuggestedItem> {
        return when {
            items.all { it.item.itemType is ItemType.Login } -> sortLoginsWithUrl(items)
            items.all { it.item.itemType is ItemType.CreditCard } -> sortCreditCards(items)
            else -> items
        }.putLastItemAutofillOnTop(lastItemAutofill)
    }

    private fun List<ItemData.SuggestedItem>.putLastItemAutofillOnTop(
        lastItemAutofillOption: Option<LastItemAutofillPreference>
    ): List<ItemData.SuggestedItem> = when (lastItemAutofillOption) {
        is Some -> {
            val lastItemAutofill = lastItemAutofillOption.value
            val lastItem = find {
                it.item.id.id == lastItemAutofill.itemId &&
                    it.item.shareId.id == lastItemAutofill.shareId
            }?.takeIf { !lastItemAutofill.isTooOld }
            lastItem?.let {
                val mutableItems = toMutableList()
                mutableItems.remove(it)
                mutableItems.add(0, it)
                mutableItems
            } ?: this
        }

        else -> this
    }

    private fun sortLoginsWithUrl(items: List<ItemData.SuggestedItem>): List<ItemData.SuggestedItem> {
        val parsed = when (val urlOption = items.firstOrNull()?.suggestion.toOption()) {
            is Some -> hostParser.parse(urlOption.value.value)
                .fold(
                    onSuccess = { it },
                    onFailure = { return items }
                )

            is None -> return items
        }

        return when (parsed) {
            is HostInfo.Ip -> items // If it's an IP, there's no sorting that we can perform
            is HostInfo.Host -> sortWithDomainInfo(parsed, items)
        }
    }

    private fun sortWithDomainInfo(
        parsed: HostInfo.Host,
        items: List<ItemData.SuggestedItem>
    ): List<ItemData.SuggestedItem> {
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
    private fun sortBySubdomain(parsed: HostInfo.Host, items: List<LoginItem>): List<ItemData.SuggestedItem> {
        val sameSubdomainItems = mutableListOf<ItemData.SuggestedItem>()
        val domainItems = mutableListOf<ItemData.SuggestedItem>()
        val otherSubdomainItems = mutableListOf<ItemData.SuggestedItem>()

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
                sameSubdomainItems.add(loginItem.suggestedItem)
            } else {
                val shouldGoToDomainItems = parsedWebsites.any {
                    it.subdomain is None && it.domain == parsed.domain
                }
                if (shouldGoToDomainItems) {
                    domainItems.add(loginItem.suggestedItem)
                } else {
                    otherSubdomainItems.add(loginItem.suggestedItem)
                }
            }
        }

        val finalList = mutableListOf<ItemData.SuggestedItem>()
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
    private fun sortByDomain(parsed: HostInfo.Host, items: List<LoginItem>): List<ItemData.SuggestedItem> {
        val domainItems = mutableListOf<ItemData.SuggestedItem>()
        val subdomainItems = mutableListOf<ItemData.SuggestedItem>()

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
                domainItems.add(loginItem.suggestedItem)
            } else {
                subdomainItems.add(loginItem.suggestedItem)
            }
        }

        val finalList = mutableListOf<ItemData.SuggestedItem>()
        finalList.addAll(domainItems)
        finalList.addAll(subdomainItems)
        return finalList
    }

    private fun getLoginItems(items: List<ItemData.SuggestedItem>): List<LoginItem> {
        val res = mutableListOf<LoginItem>()
        for (suggestedItem in items) {
            if (suggestedItem.item.itemType is ItemType.Login) {
                res.add(
                    LoginItem(
                        suggestedItem = suggestedItem,
                        login = suggestedItem.item.itemType as ItemType.Login
                    )
                )
            }
        }
        return res
    }

    private fun sortCreditCards(items: List<ItemData.SuggestedItem>): List<ItemData.SuggestedItem> {
        val creditCards = items.filter { it.item.itemType is ItemType.CreditCard }
        return creditCards.sortedByDescending {
            when (val autofillTime = it.item.lastAutofillTime) {
                None -> it.item.modificationTime
                is Some -> autofillTime.value
            }
        }
    }

    internal data class LoginItem(
        val suggestedItem: ItemData.SuggestedItem,
        val login: ItemType.Login
    )
}
