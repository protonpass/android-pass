package proton.android.pass.data.impl.autofill

import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.url.HostInfo
import proton.android.pass.data.api.url.HostParser
import proton.pass.domain.Item
import proton.pass.domain.ItemType
import javax.inject.Inject

interface SuggestionSorter {
    fun sort(items: List<Item>, url: Option<String>): List<Item>
}

class SuggestionSorterImpl @Inject constructor(
    private val hostParser: HostParser
) : SuggestionSorter {

    override fun sort(items: List<Item>, url: Option<String>): List<Item> =
        if (url is Some) {
            sortWithUrl(items, url.value)
        } else {
            items
        }

    private fun sortWithUrl(items: List<Item>, url: String): List<Item> {
        val parsed = when (val res = hostParser.parse(url)) {
            is LoadingResult.Success -> res.data
            else -> return items
        }

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
                .filterIsInstance<LoadingResult.Success<HostInfo>>()
                .map { it.data }
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
                .filterIsInstance<LoadingResult.Success<HostInfo>>()
                .map { it.data }
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
