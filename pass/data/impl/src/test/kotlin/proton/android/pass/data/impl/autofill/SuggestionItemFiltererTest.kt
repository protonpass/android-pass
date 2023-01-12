package proton.android.pass.data.impl.autofill

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.common.api.some
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestItemType
import proton.pass.domain.Item

class SuggestionItemFiltererTest {

    @Test
    fun `given an item with an allowed package name should return the suggested element`() {
        val packageName = "my.first.package.name"
        val firstItem = TestItem.create(
            itemType = TestItemType.login(),
            allowedPackageNames = listOf(packageName)
        )
        val items = listOf(
            firstItem,
            TestItem.create(
                itemType = TestItemType.login(),
                allowedPackageNames = listOf("my.second.package.name")
            )
        )

        val res = SuggestionItemFilterer.filter(items, packageName.some(), None)
        assertThat(res).isEqualTo(listOf(firstItem))
    }

    @Test
    fun `given an item with an allowed package name should return empty list on no matches`() {
        val item = TestItem.create(
            itemType = TestItemType.login(),
            allowedPackageNames = listOf("my.package.name")
        )
        val items = listOf(item)

        val res = SuggestionItemFilterer.filter(items, "my.incorrect.package.name".some(), None)
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

        val res = SuggestionItemFilterer.filter(items, None, website.some())
        assertThat(res).isEqualTo(listOf(firstItem))
    }

    @Test
    fun `given an item with a website should return empty list on no matches`() {
        val domain = "www.proton.me"
        val items = listOf(
            TestItem.create(TestItemType.login(websites = listOf(domain)))
        )

        val res = SuggestionItemFilterer.filter(items, None, "${domain}2".some())
        assertThat(res).isEqualTo(emptyList<Item>())
    }

    @Test
    fun `given an item with matching domain should return the suggestion`() {
        val baseDomain = "www.proton.me"
        val itemType = TestItemType.login(websites = listOf("https://$baseDomain/somepath"))
        val item = TestItem.create(itemType)
        val items = listOf(item)

        val res = SuggestionItemFilterer.filter(items, None, baseDomain.some())
        assertThat(res).isEqualTo(listOf(item))
    }
}
