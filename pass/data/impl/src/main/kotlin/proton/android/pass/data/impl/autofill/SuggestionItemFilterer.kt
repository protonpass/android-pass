package proton.android.pass.data.impl.autofill

import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Result
import proton.android.pass.common.api.Some
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

class SuggestionItemFiltererImpl @Inject constructor() : SuggestionItemFilterer {

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
        val urlDomain = when (val domain = UrlSanitizer.getDomain(url)) {
            is Result.Success -> domain.data
            else -> return false
        }
        val loginDomains = login.websites.map { UrlSanitizer.getDomain(it) }

        return isDomainMatch(urlDomain, loginDomains)
    }

    private fun isDomainMatch(urlDomain: String, itemDomains: List<Result<String>>): Boolean {
        val successfullySanitized = itemDomains
            .filterIsInstance<Result.Success<String>>()
            .map { it.data }



        return successfullySanitized.any {
            it == urlDomain
        }
    }
}
