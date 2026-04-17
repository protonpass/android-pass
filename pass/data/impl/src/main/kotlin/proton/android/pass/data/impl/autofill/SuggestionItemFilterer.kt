/*
 * Copyright (c) 2023-2026 Proton AG
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

import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.url.HostParser
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.domain.AutofillUrlMode
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import javax.inject.Inject

interface SuggestionItemFilterer {
    fun filter(
        items: List<Item>,
        suggestion: Suggestion,
        useAutofillUrlModes: Boolean = false
    ): List<Item>
}

class SuggestionItemFiltererImpl @Inject constructor(
    private val hostParser: HostParser
) : SuggestionItemFilterer {

    override fun filter(
        items: List<Item>,
        suggestion: Suggestion,
        useAutofillUrlModes: Boolean
    ): List<Item> = items.filter { item ->
        when (item.itemType) {
            is ItemType.Login -> isMatch(suggestion, item, useAutofillUrlModes)
            is ItemType.CreditCard -> true
            is ItemType.Identity -> true
            is ItemType.Alias,
            is ItemType.Note,
            is ItemType.Custom,
            is ItemType.WifiNetwork,
            is ItemType.SSHKey,
            ItemType.Password,
            ItemType.Unknown -> throw IllegalArgumentException("Unsupported item type")
        }
    }

    private fun isMatch(
        suggestion: Suggestion,
        item: Item,
        useAutofillUrlModes: Boolean
    ): Boolean = when (suggestion) {
        is Suggestion.PackageName -> isPackageNameMatch(suggestion.value, item)
        is Suggestion.Url -> isUrlMatch(suggestion.value, item.itemType as ItemType.Login, useAutofillUrlModes)
    }

    private fun isPackageNameMatch(packageName: String, item: Item): Boolean =
        item.packageInfoSet.map { it.packageName.value }.contains(packageName)

    private fun isUrlMatch(
        url: String,
        login: ItemType.Login,
        useAutofillUrlModes: Boolean
    ): Boolean {
        val parsedRequest = hostParser.parse(url).fold(
            onSuccess = { it },
            onFailure = { return false }
        )

        if (!useAutofillUrlModes || login.autofillUrls.isEmpty()) {
            val parsedWebsites = login.websites.mapNotNull { hostParser.parse(it).getOrNull() }
            return isLegacyMatch(parsedRequest, parsedWebsites)
        }

        return login.autofillUrls.any { autofillUrl ->
            websiteMatchesRequest(parsedRequest, autofillUrl.url, autofillUrl.mode)
        }
    }

    private fun websiteMatchesRequest(
        parsedRequest: HostInfo,
        storedWebsite: String,
        mode: AutofillUrlMode
    ): Boolean {
        if (mode == AutofillUrlMode.Never || !mode.isSupported) return false
        val parsedStored = hostParser.parse(storedWebsite).getOrNull() ?: return false
        return hostMatches(parsedRequest, parsedStored, exactSubdomain = mode == AutofillUrlMode.Exact)
    }

    private fun hostMatches(
        request: HostInfo,
        stored: HostInfo,
        exactSubdomain: Boolean
    ): Boolean = when {
        request is HostInfo.Ip && stored is HostInfo.Ip -> request.ip == stored.ip
        request is HostInfo.Host && stored is HostInfo.Host ->
            request.protocol == stored.protocol &&
                (!exactSubdomain || request.subdomain == stored.subdomain) &&
                request.domain == stored.domain &&
                request.tld == stored.tld
        else -> false
    }

    private fun isLegacyMatch(requestUrl: HostInfo, items: List<HostInfo>): Boolean =
        items.any { hostMatches(requestUrl, it, exactSubdomain = false) }
}
