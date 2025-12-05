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

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.Suggestion
import proton.android.pass.data.fakes.usecases.FakeGetPublicSuffixList
import proton.android.pass.data.impl.url.HostParserImpl
import proton.android.pass.domain.Item
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ItemTypeTestFactory

class SuggestionItemFiltererImplTest {

    private lateinit var instance: SuggestionItemFiltererImpl
    private lateinit var getPublicSuffixList: FakeGetPublicSuffixList

    @Before
    fun setup() {
        getPublicSuffixList = FakeGetPublicSuffixList()
        instance = SuggestionItemFiltererImpl(HostParserImpl(getPublicSuffixList))
    }

    @Test
    fun `given an item with an allowed package name should return the suggested element`() {
        val firstPackageInfo = PackageInfo(
            PackageName("my.first.package.name"),
            AppName("")
        )
        val secondPackageInfo = PackageInfo(
            PackageName("my.second.package.name"),
            AppName("")
        )
        val firstItem = ItemTestFactory.create(
            itemType = ItemTypeTestFactory.login(),
            packageInfoSet = setOf(firstPackageInfo)
        )
        val items = listOf(
            firstItem,
            ItemTestFactory.create(
                itemType = ItemTypeTestFactory.login(),
                packageInfoSet = setOf(secondPackageInfo)
            )
        )

        val res = instance.filter(items, Suggestion.PackageName(firstPackageInfo.packageName.value))
        assertThat(res).isEqualTo(listOf(firstItem))
    }

    @Test
    fun `given an item with an allowed package name should return empty list on no matches`() {
        val firstPackageInfo = PackageInfo(
            PackageName("my.first.package.name"),
            AppName("")
        )
        val secondPackageInfo = PackageInfo(
            PackageName("my.second.package.name"),
            AppName("")
        )
        val item = ItemTestFactory.create(
            itemType = ItemTypeTestFactory.login(),
            packageInfoSet = setOf(firstPackageInfo)
        )
        val items = listOf(item)

        val res = instance.filter(items, Suggestion.PackageName(secondPackageInfo.packageName.value))
        assertThat(res).isEqualTo(emptyList<Item>())
    }

    @Test
    fun `given an item with a website should return the suggested element`() {
        val website = "www.proton.me"
        val firstItem = ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf(website)))
        val items = listOf(
            firstItem,
            ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf("${website}2")))
        )

        val res = instance.filter(items, Suggestion.Url(website))
        assertThat(res).isEqualTo(listOf(firstItem))
    }

    @Test
    fun `given an item with a website should return empty list on no matches`() {
        val domain = "www.proton.me"
        val items = listOf(
            ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf(domain)))
        )

        val res = instance.filter(items, Suggestion.Url("${domain}2"))
        assertThat(res).isEqualTo(emptyList<Item>())
    }

    @Test
    fun `given an item with matching domain should return the suggestion`() {
        val baseDomain = "www.proton.me"
        val itemType = ItemTypeTestFactory.login(websites = listOf("https://$baseDomain/somepath"))
        val item = ItemTestFactory.create(itemType)
        val items = listOf(item)

        val res = instance.filter(items, Suggestion.Url(baseDomain))
        assertThat(res).isEqualTo(listOf(item))
    }

    @Test
    fun `check items with matching domain and tld are returned`() {
        val domain = "somedomain"
        val tld = "tld"
        val subdomain1 = "account.login"
        val subdomain2 = "account.register"

        getPublicSuffixList.setTlds(setOf(tld))
        val item1 = ItemTestFactory.create(
            ItemTypeTestFactory.login(
                websites = listOf(
                    "$subdomain1.$domain.$tld",
                    "other.random.domain"
                )
            )
        )
        val item2 = ItemTestFactory.create(
            ItemTypeTestFactory.login(
                websites = listOf(
                    "$subdomain2.$domain.$tld",
                    "some.other.site"
                )
            )
        )
        val item3 = ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf("$domain.$tld")))
        val item4 = ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf("otherdomain.$tld")))

        val items = listOf(item1, item2, item3, item4)
        val res = instance.filter(items, Suggestion.Url("$domain.$tld"))
        assertThat(res).isEqualTo(listOf(item1, item2, item3))
    }

    @Test
    fun `check items with same IP are returned`() {
        val ip = "1.2.3.4"

        val item1 = ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf(ip)))
        val item2 = ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf(ip)))
        val item3 = ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf("5.6.7.8")))

        val items = listOf(item1, item2, item3)
        val res = instance.filter(items, Suggestion.Url(ip))
        assertThat(res).isEqualTo(listOf(item1, item2))
    }

    @Test
    fun `check items with same final IP octet are not returned`() {
        val ip = "1.2.3.4"

        val item1 = ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf(ip)))
        val item2 = ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf("5.6.7.4")))

        val items = listOf(item1, item2)
        val res = instance.filter(items, Suggestion.Url(ip))
        assertThat(res).isEqualTo(listOf(item1))
    }

    @Test
    fun `check items with different protocols are not returned`() {
        val domain = "some.domain.test"
        val httpsDomain = "https://$domain"

        val item1 = ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf("ftp://$domain")))
        val item2 = ItemTestFactory.create(ItemTypeTestFactory.login(websites = listOf(httpsDomain)))

        val items = listOf(item1, item2)
        val res = instance.filter(items, Suggestion.Url(httpsDomain))
        assertThat(res).isEqualTo(listOf(item2))
    }
}
