package me.proton.pass.search

import com.google.common.truth.Truth.assertThat
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.pass.common.api.Result
import me.proton.pass.domain.Item
import me.proton.pass.domain.ItemType
import me.proton.pass.test.TestUtils.randomString
import me.proton.pass.test.crypto.TestKeyStoreCrypto
import me.proton.pass.test.domain.TestItem
import org.junit.Before
import org.junit.Test

internal class ItemFilterImplTest {

    private lateinit var itemFilter: ItemFilter

    @Before
    fun setUp() {
        itemFilter = ItemFilterImpl(TestKeyStoreCrypto)
    }

    @Test
    fun `empty query returns whole list`() {
        val itemList = listOf(TestItem.random(), TestItem.random())
        val result = itemFilter.filterByQuery(Result.Success(itemList), "")
        assertThat((result as Result.Success).data).isEqualTo(itemList)
    }

    @Test
    fun `query that matches items returns the matching items`() {
        val title = randomString()
        val item1 = TestItem.random(title = title)
        val item2 = TestItem.random(title = "$title-abc")

        val result = itemFilter.filterByQuery(
            Result.Success(listOf(item1, item2)),
            title
        )

        val allItems = (result as Result.Success).data
        assertThat(allItems.size).isEqualTo(2)
        assertThat(allItems[0]).isEqualTo(item1)
        assertThat(allItems[1]).isEqualTo(item2)

        val result2 = itemFilter.filterByQuery(
            Result.Success(listOf(item1, item2)),
            "$title-"
        )

        val filteredItems = (result2 as Result.Success).data
        assertThat(filteredItems.size).isEqualTo(1)
        assertThat(filteredItems[0]).isEqualTo(item2)
    }

    @Test
    fun `search is case insensitive`() {
        val item1 = TestItem.random(title = "ABc")
        val item2 = TestItem.random(title = "aBc")
        val result = itemFilter.filterByQuery(
            Result.Success(listOf(item1, item2)),
            "ab"
        )

        val filteredItems = (result as Result.Success).data
        assertThat(filteredItems.size).isEqualTo(2)
        assertThat(filteredItems[0]).isEqualTo(item1)
        assertThat(filteredItems[1]).isEqualTo(item2)
    }

    @Test
    fun `login item filtered by title`() {
        val title = randomString()
        runTestWithItemAndQuery(
            item = TestItem.random(
                title = title,
                itemType = ItemType.Login(
                    username = randomString(),
                    password = randomString().encrypt(TestKeyStoreCrypto),
                    websites = emptyList()
                )
            ),
            query = title
        )
    }

    @Test
    fun `login item filtered by note`() {
        val note = randomString()
        runTestWithItemAndQuery(
            item = TestItem.random(
                note = note,
                itemType = ItemType.Login(
                    username = randomString(),
                    password = randomString().encrypt(TestKeyStoreCrypto),
                    websites = emptyList()
                )
            ),
            query = note
        )
    }

    @Test
    fun `login item filtered by website`() {
        val website = randomString()
        runTestWithItemAndQuery(
            item = TestItem.random(
                itemType = ItemType.Login(
                    username = randomString(),
                    password = randomString().encrypt(TestKeyStoreCrypto),
                    websites = listOf(website)
                )
            ),
            query = website
        )
    }

    @Test
    fun `note item filtered by title`() {
        val title = randomString()
        runTestWithItemAndQuery(
            item = TestItem.random(
                title = title,
                itemType = ItemType.Note(
                    text = randomString()
                )
            ),
            query = title
        )
    }

    @Test
    fun `note item filtered by note`() {
        val note = randomString()
        runTestWithItemAndQuery(
            item = TestItem.random(
                title = randomString(),
                note = randomString(),
                itemType = ItemType.Note(
                    text = note
                )
            ),
            query = note
        )
    }

    @Test
    fun `alias item filtered by title`() {
        val title = randomString()
        runTestWithItemAndQuery(
            item = TestItem.random(
                title = title,
                note = randomString(),
                itemType = ItemType.Alias(
                    aliasEmail = randomString()
                )
            ),
            query = title
        )
    }

    @Test
    fun `alias item filtered by note`() {
        val note = randomString()
        runTestWithItemAndQuery(
            item = TestItem.random(
                title = randomString(),
                note = note,
                itemType = ItemType.Alias(
                    aliasEmail = randomString()
                )
            ),
            query = note
        )
    }

    @Test
    fun `alias item filtered by alias`() {
        val alias = randomString()
        runTestWithItemAndQuery(
            item = TestItem.random(
                title = randomString(),
                note = randomString(),
                itemType = ItemType.Alias(
                    aliasEmail = alias
                )
            ),
            query = alias
        )
    }

    private fun runTestWithItemAndQuery(item: Item, query: String) {
        val result = itemFilter.filterByQuery(
            Result.Success(listOf(item)),
            query
        )
        val filteredItems = (result as Result.Success).data
        assertThat(filteredItems.size).isEqualTo(1)
        assertThat(filteredItems[0]).isEqualTo(item)

        val result2 = itemFilter.filterByQuery(
            Result.Success(listOf(item)),
            "${query}abc"
        )
        val emptyItems = (result2 as Result.Success).data
        assertThat(emptyItems).isEmpty()
    }
}
