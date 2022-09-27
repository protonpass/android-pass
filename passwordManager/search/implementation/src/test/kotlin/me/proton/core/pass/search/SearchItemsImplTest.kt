package me.proton.core.pass.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemType
import me.proton.core.pass.test.TestUtils.randomString
import me.proton.core.pass.test.crypto.TestKeyStoreCrypto
import me.proton.core.pass.test.domain.TestItem
import me.proton.core.pass.test.domain.usecases.TestObserveActiveItems
import org.junit.Before
import org.junit.Test

internal class SearchItemsImplTest {

    private lateinit var observeActiveItems: TestObserveActiveItems
    private lateinit var search: SearchItems

    @Before
    fun setUp() {
        observeActiveItems = TestObserveActiveItems()
        search = SearchItemsImpl(TestKeyStoreCrypto, observeActiveItems)
    }

    @Test
    fun `empty query returns whole list`() =
        runTest {
            val itemList = listOf(TestItem.random(), TestItem.random())
            observeActiveItems.sendItemList(itemList)

            val flow = search.observeResults()
            search.updateQuery("")
            flow.test {
                val items = awaitItem()
                assertThat(items).isEqualTo(itemList)
            }
        }

    @Test
    fun `query that matches items returns the matching items`() =
        runTest {
            val title = randomString()
            val item1 = TestItem.random(title = title)
            val item2 = TestItem.random(title = "$title-abc")

            observeActiveItems.sendItemList(listOf(item1, item2))

            val flow = search.observeResults()

            search.updateQuery(title)
            flow.test {
                val allItems = awaitItem()
                assertThat(allItems.size).isEqualTo(2)
                assertThat(allItems[0]).isEqualTo(item1)
                assertThat(allItems[1]).isEqualTo(item2)

                search.updateQuery("$title-")

                val filteredItems = awaitItem()
                assertThat(filteredItems.size).isEqualTo(1)
                assertThat(filteredItems[0]).isEqualTo(item2)
            }
        }

    @Test
    fun `emitting new items updates the flow`() =
        runTest {
            val title = randomString()
            val item1 = TestItem.random(title = title)
            val item2 = TestItem.random()

            observeActiveItems.sendItemList(emptyList())
            search.updateQuery(title)

            val receiverFlow = search.observeResults()
            receiverFlow.test {
                assertThat(awaitItem()).isEmpty() // Skip first

                observeActiveItems.sendItemList(listOf(item1, item2))

                val items = awaitItem()
                assertThat(items).isNotEmpty()
                assertThat(items.size).isEqualTo(1)
                assertThat(items[0]).isEqualTo(item1)
            }
        }

    @Test
    fun `clearing the search emits all the values`() =
        runTest {
            val item1 = TestItem.random(title = "abc")
            val item2 = TestItem.random(title = "def")
            observeActiveItems.sendItemList(listOf(item1, item2))
            search.updateQuery("")

            search.observeResults().test {
                val allItems = awaitItem()
                assertThat(allItems.size).isEqualTo(2)

                search.updateQuery("ghi")
                val noItems = awaitItem()
                assertThat(noItems).isEmpty()

                search.clearSearch()
                val allItemsAgain = awaitItem()
                assertThat(allItemsAgain.size).isEqualTo(2)
                assertThat(allItemsAgain[0]).isEqualTo(item1)
                assertThat(allItemsAgain[1]).isEqualTo(item2)
            }
        }

    @Test
    fun `search is case insensitive`() =
        runTest {
            val item1 = TestItem.random(title = "ABc")
            val item2 = TestItem.random(title = "aBc")
            observeActiveItems.sendItemList(listOf(item1, item2))
            search.updateQuery("ab")

            search.observeResults().test {
                val filteredItems = awaitItem()
                assertThat(filteredItems.size).isEqualTo(2)
                assertThat(filteredItems[0]).isEqualTo(item1)
                assertThat(filteredItems[1]).isEqualTo(item2)
            }
        }

    @Test
    fun `login item filtered by title`() =
        runTest {
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
    fun `login item filtered by note`() =
        runTest {
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
    fun `login item filtered by website`() =
        runTest {
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
    fun `note item filtered by title`() =
        runTest {
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
    fun `note item filtered by note`() =
        runTest {
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
    fun `alias item filtered by title`() =
        runTest {
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
    fun `alias item filtered by note`() =
        runTest {
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
    fun `alias item filtered by alias`() =
        runTest {
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

    private suspend fun runTestWithItemAndQuery(item: Item, query: String) {
        observeActiveItems.sendItemList(listOf(item))
        search.updateQuery(query)

        search.observeResults().test {
            val filteredItems = awaitItem()
            assertThat(filteredItems.size).isEqualTo(1)
            assertThat(filteredItems[0]).isEqualTo(item)

            search.updateQuery("${query}abc")
            val emptyItems = awaitItem()
            assertThat(emptyItems).isEmpty()
        }
    }

}
