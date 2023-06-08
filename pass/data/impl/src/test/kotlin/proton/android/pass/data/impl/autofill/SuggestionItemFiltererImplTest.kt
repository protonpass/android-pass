package proton.android.pass.data.impl.autofill

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.data.fakes.usecases.TestGetPublicSuffixList
import proton.android.pass.data.impl.url.HostParserImpl
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestItemType
import proton.pass.domain.Item
import proton.pass.domain.entity.AppName
import proton.pass.domain.entity.PackageInfo
import proton.pass.domain.entity.PackageName

class SuggestionItemFiltererImplTest {

    private lateinit var instance: SuggestionItemFiltererImpl
    private lateinit var getPublicSuffixList: TestGetPublicSuffixList

    @Before
    fun setup() {
        getPublicSuffixList = TestGetPublicSuffixList()
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
        val firstItem = TestItem.create(
            itemType = TestItemType.login(),
            packageInfoSet = setOf(firstPackageInfo)
        )
        val items = listOf(
            firstItem,
            TestItem.create(
                itemType = TestItemType.login(),
                packageInfoSet = setOf(secondPackageInfo)
            )
        )

        val res = instance.filter(items, firstPackageInfo.packageName.value.some(), None)
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
        val item = TestItem.create(
            itemType = TestItemType.login(),
            packageInfoSet = setOf(firstPackageInfo)
        )
        val items = listOf(item)

        val res = instance.filter(items, secondPackageInfo.packageName.value.some(), None)
        assertThat(res).isEqualTo(emptyList<Item>())
    }

    @Test
    fun `given an item with a website should return the suggested element`() {
        val website = "www.proton.me"
        val firstItem = TestItem.create(TestItemType.login(websites = listOf(website)))
        val items = listOf(
            firstItem,
            TestItem.create(TestItemType.login(websites = listOf("${website}2")))
        )

        val res = instance.filter(items, None, website.some())
        assertThat(res).isEqualTo(listOf(firstItem))
    }

    @Test
    fun `given an item with a website should return empty list on no matches`() {
        val domain = "www.proton.me"
        val items = listOf(
            TestItem.create(TestItemType.login(websites = listOf(domain)))
        )

        val res = instance.filter(items, None, "${domain}2".some())
        assertThat(res).isEqualTo(emptyList<Item>())
    }

    @Test
    fun `given an item with matching domain should return the suggestion`() {
        val baseDomain = "www.proton.me"
        val itemType = TestItemType.login(websites = listOf("https://$baseDomain/somepath"))
        val item = TestItem.create(itemType)
        val items = listOf(item)

        val res = instance.filter(items, None, baseDomain.some())
        assertThat(res).isEqualTo(listOf(item))
    }

    @Test
    fun `check items with matching domain and tld are returned`() {
        val domain = "somedomain"
        val tld = "tld"
        val subdomain1 = "account.login"
        val subdomain2 = "account.register"

        getPublicSuffixList.setTlds(setOf(tld))
        val item1 = TestItem.create(
            TestItemType.login(
                websites = listOf(
                    "$subdomain1.$domain.$tld",
                    "other.random.domain"
                )
            )
        )
        val item2 = TestItem.create(
            TestItemType.login(
                websites = listOf(
                    "$subdomain2.$domain.$tld",
                    "some.other.site"
                )
            )
        )
        val item3 = TestItem.create(TestItemType.login(websites = listOf("$domain.$tld")))
        val item4 = TestItem.create(TestItemType.login(websites = listOf("otherdomain.$tld")))

        val items = listOf(item1, item2, item3, item4)
        val res = instance.filter(items, None, "$domain.$tld".some())
        assertThat(res).isEqualTo(listOf(item1, item2, item3))
    }

    @Test
    fun `check items with same IP are returned`() {
        val ip = "1.2.3.4"

        val item1 = TestItem.create(TestItemType.login(websites = listOf(ip)))
        val item2 = TestItem.create(TestItemType.login(websites = listOf(ip)))
        val item3 = TestItem.create(TestItemType.login(websites = listOf("5.6.7.8")))

        val items = listOf(item1, item2, item3)
        val res = instance.filter(items, None, ip.some())
        assertThat(res).isEqualTo(listOf(item1, item2))
    }

    @Test
    fun `check items with same final IP octet are not returned`() {
        val ip = "1.2.3.4"

        val item1 = TestItem.create(TestItemType.login(websites = listOf(ip)))
        val item2 = TestItem.create(TestItemType.login(websites = listOf("5.6.7.4")))

        val items = listOf(item1, item2)
        val res = instance.filter(items, None, ip.some())
        assertThat(res).isEqualTo(listOf(item1))
    }

    @Test
    fun `check items with different protocols are not returned`() {
        val domain = "some.domain.test"
        val httpsDomain = "https://$domain"

        val item1 = TestItem.create(TestItemType.login(websites = listOf("ftp://$domain")))
        val item2 = TestItem.create(TestItemType.login(websites = listOf(httpsDomain)))

        val items = listOf(item1, item2)
        val res = instance.filter(items, None, url = httpsDomain.some())
        assertThat(res).isEqualTo(listOf(item2))
    }
}
