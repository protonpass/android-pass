package proton.android.pass.data.impl.usecases

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Result
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.fakes.usecases.TestObserveActiveItems
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.crypto.TestKeyStoreCrypto
import proton.android.pass.test.domain.TestItem
import proton.pass.domain.Item
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private typealias Filter = (Item) -> Boolean

class FakeSuggestionItemFilterer : SuggestionItemFilterer {
    private var filter: Filter = {
        throw IllegalStateException("Filter has not been initialized")
    }

    fun setFilter(fn: Filter) {
        this.filter = fn
    }

    override fun filter(
        items: List<Item>,
        packageName: Option<String>,
        url: Option<String>
    ): List<Item> = items.filter { filter.invoke(it) }
}

class FakeSuggestionSorter : SuggestionSorter {
    override fun sort(items: List<Item>, url: Option<String>): List<Item> = items
}

@RunWith(JUnit4::class)
class GetSuggestedLoginItemsImplTest {

    @get:Rule
    val dispatcher = MainDispatcherRule()

    private lateinit var observeActiveItems: TestObserveActiveItems
    private lateinit var filter: FakeSuggestionItemFilterer
    private lateinit var getSuggestedLoginItems: GetSuggestedLoginItems

    @Before
    fun setUp() {
        observeActiveItems = TestObserveActiveItems()
        filter = FakeSuggestionItemFilterer()
        getSuggestedLoginItems = GetSuggestedLoginItemsImpl(
            observeActiveItems,
            filter,
            FakeSuggestionSorter()
        )
    }

    @Test
    fun `filter is invoked`() = runTest {
        val fixedTitle = "item1"
        val item1 = TestItem.random(title = fixedTitle)
        val item2 = TestItem.random()


        observeActiveItems.sendItemList(Result.Success(listOf(item1, item2)))
        filter.setFilter { TestKeyStoreCrypto.decrypt(it.title) == fixedTitle }

        getSuggestedLoginItems.invoke(None, None).test {
            val res = awaitItem()
            assertTrue(res is Result.Success)
            assertEquals(res.data, listOf(item1))
        }
    }

    @Test
    fun `error is propagated`() = runTest {
        val message = "test exception"

        filter.setFilter { true }
        observeActiveItems.sendItemList(Result.Error(Exception(message)))

        getSuggestedLoginItems.invoke(None, None).test {
            val res = awaitItem()
            assertTrue(res is Result.Error)

            val e = res.exception
            assertNotNull(e)
            assertEquals(e.message, message)
        }
    }
}

