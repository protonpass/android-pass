package me.proton.core.pass.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
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

}
