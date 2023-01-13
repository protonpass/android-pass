package proton.android.pass.data.impl.autofill

import org.junit.Before
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.data.fakes.usecases.TestGetPublicSuffixList
import proton.android.pass.data.impl.url.HostParserImpl
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestItemType
import kotlin.test.assertEquals

class SuggestionSorterImplTest {

    private lateinit var publicSuffixList: TestGetPublicSuffixList
    private lateinit var instance: SuggestionSorterImpl

    @Before
    fun setup() {
        publicSuffixList = TestGetPublicSuffixList()
        instance = SuggestionSorterImpl(HostParserImpl(publicSuffixList))
    }

    @Test
    fun `none url just returns the same list`() {
        val list = listOf(
            TestItem.random(TestItemType.login()),
            TestItem.random(TestItemType.login())
        )

        val res = instance.sort(list, None)
        assertEquals(res, list)
    }

    @Test
    fun `invalid url just returns the same list`() {
        val list = listOf(
            TestItem.random(TestItemType.login()),
            TestItem.random(TestItemType.login())
        )

        val res = instance.sort(list, "some invalid url".some())
        assertEquals(res, list)
    }

    @Test
    fun `using an ip returns the same list`() {
        val ip = "1.2.3.4"
        val list = listOf(
            TestItem.random(TestItemType.login(websites = listOf(ip))),
            TestItem.random(TestItemType.login(websites = listOf(ip)))
        )

        val res = instance.sort(list, ip.some())
        assertEquals(res, list)
    }

    @Test
    fun `sort with domain`() {
        val tld = "sometld"
        val domain = "somedomain.$tld"
        val item1 = TestItem.random(TestItemType.login(websites = listOf("subd.$domain")))
        val item2 = TestItem.random(TestItemType.login(websites = listOf("a.b.$domain")))
        val item3 = TestItem.random(TestItemType.login(websites = listOf(domain)))
        publicSuffixList.setTlds(setOf(tld))


        // The result should be [item3, item1, item2] as we want the domain match before the
        // subdomain when we filtered by domain
        val res = instance.sort(listOf(item1, item2, item3), domain.some())
        assertEquals(res, listOf(item3, item1, item2))
    }

    @Test
    fun `sort with subdomain`() {
        val tld = "sometld"
        val domain = "somedomain.$tld"
        val subdomain = "somesubdomain.$domain"
        val item1 = TestItem.random(TestItemType.login(websites = listOf("other.$domain")))
        val item2 = TestItem.random(TestItemType.login(websites = listOf("random.$domain")))
        val item3 = TestItem.random(TestItemType.login(websites = listOf(subdomain)))
        val item4 = TestItem.random(TestItemType.login(websites = listOf(domain)))
        publicSuffixList.setTlds(setOf(tld))


        // The result should be [item3, item4, item1, item2] as we want the order:
        // - exact subdomain match
        // - only base domain
        // - rest of subdomains
        val items = listOf(item1, item2, item3, item4)
        val res = instance.sort(items, subdomain.some())
        assertEquals(res, listOf(item3, item4, item1, item2))
    }

}
