package me.proton.pass.domain.usecases

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.pass.common.api.None
import me.proton.pass.common.api.Result
import me.proton.pass.common.api.some
import me.proton.pass.domain.Item
import me.proton.pass.test.domain.TestItem
import me.proton.pass.test.domain.TestItemType
import me.proton.pass.test.domain.usecases.TestObserveActiveItems
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class GetSuggestedLoginItemsImplTest {

    private lateinit var observeActiveItems: TestObserveActiveItems
    lateinit var getSuggestedLoginItems: GetSuggestedLoginItems

    @Before
    fun setUp() {
        observeActiveItems = TestObserveActiveItems()
        getSuggestedLoginItems = GetSuggestedLoginItemsImpl(observeActiveItems)
    }

    @Test
    fun `given an item with an allowed package name should return the suggested element`() =
        runTest {
            val firstItemType = TestItemType.login()
            val secondItemType = TestItemType.login()
            val firstItem = TestItem.create(
                itemType = firstItemType,
                allowedPackageNames = listOf("my.first.package.name")
            )
            val secondItem = TestItem.create(
                itemType = secondItemType,
                allowedPackageNames = listOf("my.second.package.name")
            )
            val successResult: Result<List<Item>> = Result.Success(listOf(firstItem, secondItem))
            observeActiveItems.sendItemList(successResult)
            getSuggestedLoginItems.invoke("my.first.package.name".some(), None)
                .test {
                    assertThat(awaitItem())
                        .isEqualTo(Result.Success(listOf(firstItem)))
                }
        }

    @Test
    fun `given an item with an allowed package name should return empty list on no matches`() =
        runTest {
            val itemType = TestItemType.login()
            val item = TestItem.create(
                itemType = itemType,
                allowedPackageNames = listOf("my.package.name")
            )
            val successResult: Result<List<Item>> = Result.Success(listOf(item))
            observeActiveItems.sendItemList(successResult)
            getSuggestedLoginItems.invoke("my.incorrect.package.name".some(), None)
                .test {
                    assertThat(awaitItem())
                        .isEqualTo(Result.Success(emptyList<Item>()))
                }
        }

    @Test
    fun `given an item with a website should return the suggested element`() =
        runTest {
            val firstItemType =
                TestItemType.login(websites = listOf("www.proton.me"))
            val secondItemType =
                TestItemType.login(websites = listOf("www.proton.me.2"))
            val firstItem = TestItem.create(firstItemType)
            val successResult: Result<List<Item>> = Result.Success(
                listOf(firstItem, TestItem.create(secondItemType))
            )
            observeActiveItems.sendItemList(successResult)
            getSuggestedLoginItems.invoke(None, "www.proton.me".some())
                .test {
                    assertThat(awaitItem())
                        .isEqualTo(Result.Success(listOf(firstItem)))
                }
        }

    @Test
    fun `given an item with a website should return empty list on no matches`() =
        runTest {
            val itemType = TestItemType.login(websites = listOf("www.proton.me"))
            val item = TestItem.create(itemType)
            val successResult: Result<List<Item>> = Result.Success(listOf(item))
            observeActiveItems.sendItemList(successResult)
            getSuggestedLoginItems.invoke(None, "www.proton.me.2".some())
                .test {
                    assertThat(awaitItem())
                        .isEqualTo(Result.Success(emptyList<Item>()))
                }
        }
}
