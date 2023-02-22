package proton.android.pass.data.impl.autofill

import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.url.HostParser
import proton.android.pass.data.api.url.UrlSanitizer
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
        item.allowedPackageNames.contains(packageName)

    private fun isUrlMatch(url: String, login: ItemType.Login): Boolean {
        val urlDomain = UrlSanitizer.getDomain(url).fold(
            onSuccess = { it },
            onFailure = { return false }
        )
        val loginDomains = login.websites
            .map { UrlSanitizer.getDomain(it) }
            .filter { it.isSuccess }
            .mapNotNull { it.getOrNull() }

        return isDomainMatch(urlDomain, loginDomains)
    }

    private fun isDomainMatch(urlDomain: String, itemDomains: List<String>): Boolean {
        val parsedItemDomains = parseItemDomains(itemDomains)
        val parsedDomain = hostParser.parse(urlDomain).fold(
            onSuccess = { it },
            onFailure = { return false }
        )

        return parsedItemDomains.any {
            when (it) {
                is HostInfo.Ip -> when (parsedDomain) {
                    is HostInfo.Ip -> it.ip == parsedDomain.ip
                    else -> false
                }
                is HostInfo.Host -> when (parsedDomain) {
                    is HostInfo.Host -> {
                        parsedDomain.tld == it.tld && parsedDomain.domain == it.domain
                    }
                    else -> false
                }
            }
        }
    }

    private fun parseItemDomains(itemDomains: List<String>): List<HostInfo> =
        itemDomains
            .map { hostParser.parse(it) }
            .filterIsInstance<LoadingResult.Success<HostInfo>>()
            .map { it.data }
}
