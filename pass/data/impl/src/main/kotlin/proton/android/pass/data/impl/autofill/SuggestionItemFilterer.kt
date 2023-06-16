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

import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.url.HostParser
import proton.pass.domain.Item
import proton.pass.domain.ItemType
import javax.inject.Inject

interface SuggestionItemFilterer {
    fun filter(
        items: List<Item>,
        packageName: Option<String>,
        url: Option<String>
    ): List<Item>
}

class SuggestionItemFiltererImpl @Inject constructor(
    private val hostParser: HostParser
) : SuggestionItemFilterer {

    override fun filter(
        items: List<Item>,
        packageName: Option<String>,
        url: Option<String>
    ): List<Item> = items.filter {
        if (it.itemType is ItemType.Login) {
            isMatch(packageName, url, it, it.itemType as ItemType.Login)
        } else {
            false
        }
    }

    private fun isMatch(
        packageName: Option<String>,
        url: Option<String>,
        item: Item,
        login: ItemType.Login
    ): Boolean =
        if (packageName is Some) {
            isPackageNameMatch(packageName.value, item)
        } else if (url is Some) {
            isUrlMatch(url.value, login)
        } else false

    private fun isPackageNameMatch(packageName: String, item: Item): Boolean =
        item.packageInfoSet.map { it.packageName.value }.contains(packageName)

    private fun isUrlMatch(url: String, login: ItemType.Login): Boolean {
        val parsedUrl = hostParser.parse(url).fold(
            onSuccess = { it },
            onFailure = { return false }
        )

        val parsedWebsites = login.websites
            .map { hostParser.parse(it) }
            .filter { it.isSuccess }
            .mapNotNull { it.getOrNull() }

        return isMatch(parsedUrl, parsedWebsites)
    }

    private fun isMatch(requestUrl: HostInfo, items: List<HostInfo>): Boolean =
        items.any {
            when (it) {
                is HostInfo.Ip -> when (requestUrl) {
                    is HostInfo.Ip -> it.ip == requestUrl.ip
                    else -> false
                }

                is HostInfo.Host -> when (requestUrl) {
                    is HostInfo.Host -> {
                        requestUrl.protocol == it.protocol &&
                            requestUrl.tld == it.tld &&
                            requestUrl.domain == it.domain
                    }

                    else -> false
                }
            }
        }
}
