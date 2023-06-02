package proton.android.pass.data.impl.usecases

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.data.api.usecases.GetSuggestedLoginItems
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveActiveItems
import proton.android.pass.data.fakes.usecases.TestObserveVaults
import proton.android.pass.data.impl.autofill.SuggestionItemFilterer
import proton.android.pass.data.impl.autofill.SuggestionSorter
import proton.android.pass.test.MainDispatcherRule
import proton.android.pass.test.domain.TestItem
import proton.pass.domain.Item
import proton.pass.domain.Plan
import proton.pass.domain.PlanLimit
import proton.pass.domain.PlanType
import proton.pass.domain.ShareId
import proton.pass.domain.ShareSelection
import proton.pass.domain.Vault
import kotlin.test.assertEquals
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
    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var observeVaults: TestObserveVaults

    @Before
    fun setUp() {
        observeActiveItems = TestObserveActiveItems()
        filter = FakeSuggestionItemFilterer()
        getUserPlan = TestGetUserPlan()
        observeVaults = TestObserveVaults()
        getSuggestedLoginItems = GetSuggestedLoginItemsImpl(
            getUserPlan = getUserPlan,
            observeVaults = observeVaults,
            observeActiveItems = observeActiveItems,
            suggestionItemFilter = filter,
            suggestionSorter = FakeSuggestionSorter()
        )
    }

    @Test
    fun `filter is invoked`() = runTest {
        emitDefaultVaultAndPlan()

        val fixedTitle = "item1"
        val item1 = TestItem.random(title = fixedTitle)
        val item2 = TestItem.random()


        observeActiveItems.sendItemList(listOf(item1, item2))
        filter.setFilter { TestKeyStoreCrypto.decrypt(it.title) == fixedTitle }

        getSuggestedLoginItems.invoke(None, None).test {
            assertEquals(awaitItem(), listOf(item1))
        }

        val memory = observeActiveItems.getMemory()
        val expected = TestObserveActiveItems.Payload(
            filter = ItemTypeFilter.Logins,
            shareSelection = ShareSelection.AllShares
        )
        assertThat(memory).isEqualTo(listOf(expected))
    }

    @Test
    fun `error is propagated`() = runTest {
        emitDefaultVaultAndPlan()

        val message = "test exception"

        filter.setFilter { true }
        observeActiveItems.sendException(Exception(message))

        getSuggestedLoginItems.invoke(None, None).test {
            val e = awaitError()
            assertTrue(e is Exception)
            assertEquals(e.message, message)
        }

        val memory = observeActiveItems.getMemory()
        val expected = TestObserveActiveItems.Payload(
            filter = ItemTypeFilter.Logins,
            shareSelection = ShareSelection.AllShares
        )
        assertThat(memory).isEqualTo(listOf(expected))
    }

    @Test
    fun `only suggestions from the primary vault if plan is free`() = runTest {
        // GIVEN
        val primaryShareId = ShareId("123")
        val vaults = listOf(
            Vault(
                shareId = primaryShareId,
                name = "default",
                isPrimary = true
            ),
            Vault(
                shareId = ShareId("456"),
                name = "other",
                isPrimary = false
            )
        )
        observeVaults.sendResult(Result.success(vaults))
        getUserPlan.setResult(Result.success(createPlan(PlanType.Free)))

        filter.setFilter { true }

        val items = listOf(TestItem.random())
        observeActiveItems.sendItemList(items)

        // WHEN
        val res = getSuggestedLoginItems.invoke(None, None).first()

        // THEN
        assertThat(res).isEqualTo(items)

        val memory = observeActiveItems.getMemory()
        val expected = TestObserveActiveItems.Payload(
            filter = ItemTypeFilter.Logins,
            shareSelection = ShareSelection.Share(primaryShareId)
        )
        assertThat(memory).isEqualTo(listOf(expected))
    }

    private fun emitDefaultVaultAndPlan() {
        val defaultVault = Vault(
            shareId = ShareId("123"),
            name = "default",
            isPrimary = true
        )
        observeVaults.sendResult(Result.success(listOf(defaultVault)))
        getUserPlan.setResult(Result.success(createPlan(PlanType.Paid("", ""))))
    }

    private fun createPlan(planType: PlanType) = Plan(
        planType = planType,
        hideUpgrade = false,
        vaultLimit = PlanLimit.Limited(1),
        aliasLimit = PlanLimit.Limited(1),
        totpLimit = PlanLimit.Limited(1),
        updatedAt = Clock.System.now().epochSeconds
    )
}

