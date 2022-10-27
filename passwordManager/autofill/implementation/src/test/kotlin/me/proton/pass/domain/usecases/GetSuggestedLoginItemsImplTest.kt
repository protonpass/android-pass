package me.proton.pass.domain.usecases

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.pass.common.api.Result
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
            val firstItemType =
                TestItemType.login(allowedPackageNames = listOf("my.first.package.name"))
            val secondItemType =
                TestItemType.login(allowedPackageNames = listOf("my.second.package.name"))
            val firstItem = TestItem.create(firstItemType)
            val successResult: Result<List<Item>> = Result.Success(
                listOf(firstItem, TestItem.create(secondItemType))
            )
            observeActiveItems.sendItemList(successResult)
            getSuggestedLoginItems.invoke(UrlOrPackage("my.first.package.name"))
                .test {
                    assertThat(awaitItem())
                        .isEqualTo(Result.Success(listOf(firstItem)))
                }
        }

    @Test
    fun `given an item with an allowed package name should return empty list on no matches`() =
        runTest {
            val itemType = TestItemType.login(allowedPackageNames = listOf("my.package.name"))
            val item = TestItem.create(itemType)
            val successResult: Result<List<Item>> = Result.Success(listOf(item))
            observeActiveItems.sendItemList(successResult)
            getSuggestedLoginItems.invoke(UrlOrPackage("my.incorrect.package.name"))
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
            getSuggestedLoginItems.invoke(UrlOrPackage("www.proton.me"))
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
            getSuggestedLoginItems.invoke(UrlOrPackage("www.proton.me.2"))
                .test {
                    assertThat(awaitItem())
                        .isEqualTo(Result.Success(emptyList<Item>()))
                }
        }
}
