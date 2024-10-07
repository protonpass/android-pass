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

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.url.HostParser
import proton.android.pass.data.api.usecases.SuggestedItem
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.domain.ItemType
import proton.android.pass.preferences.LastItemAutofillPreference
import javax.inject.Inject

interface SuggestionSorter {
    fun sort(items: List<SuggestedItem>, lastItemAutofill: Option<LastItemAutofillPreference>): List<SuggestedItem>
}

class SuggestionSorterImpl @Inject constructor(
    private val hostParser: HostParser
) : SuggestionSorter {

    override fun sort(
        items: List<SuggestedItem>,
        lastItemAutofill: Option<LastItemAutofillPreference>
    ): List<SuggestedItem> {
        return when {
            items.all { it.itemType is ItemType.Login } -> sortLoginsWithUrl(items)
            items.all { it.itemType is ItemType.CreditCard } -> sortCreditCards(items)
            else -> items
        }.putLastItemAutofillOnTop(lastItemAutofill)
    }

    private fun List<SuggestedItem>.putLastItemAutofillOnTop(
        lastItemAutofillOption: Option<LastItemAutofillPreference>
    ): List<SuggestedItem> = when (lastItemAutofillOption) {
        is Some -> {
            val lastItemAutofill = lastItemAutofillOption.value
            val lastItem = find {
                it.id.id == lastItemAutofill.itemId &&
                    it.shareId.id == lastItemAutofill.shareId
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

    private fun sortLoginsWithUrl(items: List<SuggestedItem>): List<SuggestedItem> {
        val suggestionOption: Option<Suggestion> = items.firstOrNull()?.suggestion ?: None
        val urlOption: Option<Suggestion.Url> =
            if (suggestionOption is Some && suggestionOption.value is Suggestion.Url) {
                Some(suggestionOption.value as Suggestion.Url)
            } else {
                None
            }
        val parsed = when (urlOption) {
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

    private fun sortWithDomainInfo(parsed: HostInfo.Host, items: List<SuggestedItem>): List<SuggestedItem> {
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
    private fun sortBySubdomain(parsed: HostInfo.Host, items: List<LoginItem>): List<SuggestedItem> {
        val sameSubdomainItems = mutableListOf<SuggestedItem>()
        val domainItems = mutableListOf<SuggestedItem>()
        val otherSubdomainItems = mutableListOf<SuggestedItem>()

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

        val finalList = mutableListOf<SuggestedItem>()
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
    private fun sortByDomain(parsed: HostInfo.Host, items: List<LoginItem>): List<SuggestedItem> {
        val domainItems = mutableListOf<SuggestedItem>()
        val subdomainItems = mutableListOf<SuggestedItem>()

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

        val finalList = mutableListOf<SuggestedItem>()
        finalList.addAll(domainItems)
        finalList.addAll(subdomainItems)
        return finalList
    }

    private fun getLoginItems(items: List<SuggestedItem>): List<LoginItem> {
        val res = mutableListOf<LoginItem>()
        for (suggestedItem in items) {
            if (suggestedItem.itemType is ItemType.Login) {
                res.add(
                    LoginItem(
                        suggestedItem = suggestedItem,
                        login = suggestedItem.itemType as ItemType.Login
                    )
                )
            }
        }
        return res
    }

    private fun sortCreditCards(items: List<SuggestedItem>): List<SuggestedItem> {
        val creditCards = items.filter { it.itemType is ItemType.CreditCard }
        return creditCards.sortedByDescending {
            when (val autofillTime = it.lastAutofillTime) {
                None -> it.modificationTime
                is Some -> autofillTime.value
            }
        }
    }

    internal data class LoginItem(
        val suggestedItem: SuggestedItem,
        val login: ItemType.Login
    )
}
