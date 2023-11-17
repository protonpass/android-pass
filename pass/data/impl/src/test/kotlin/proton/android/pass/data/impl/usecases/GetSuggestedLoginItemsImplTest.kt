/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

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
import proton.android.pass.test.TestConstants
import proton.android.pass.test.domain.TestItem
import proton.android.pass.domain.Item
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.Vault
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private typealias Filter = (proton.android.pass.domain.Item) -> Boolean

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
    fun `only suggestions from the writeable vaults if plan is free`() = runTest {
        // GIVEN
        val firstShareId = ShareId("123")
        val secondShareId = ShareId("456")
        val vaults = listOf(
            Vault(
                shareId = firstShareId,
                name = "default",
                role = ShareRole.Admin
            ),
            Vault(
                shareId = secondShareId,
                name = "other",
                role = ShareRole.Admin
            ),
            Vault(
                shareId = ShareId("789"),
                name = "another",
                role = ShareRole.Read
            )
        )
        observeVaults.sendResult(Result.success(vaults))
        getUserPlan.setResult(Result.success(createPlan(TestConstants.FreePlanType)))

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
            shareSelection = ShareSelection.Shares(listOf(firstShareId, secondShareId))
        )
        assertThat(memory).isEqualTo(listOf(expected))
    }

    private fun emitDefaultVaultAndPlan() {
        val defaultVault = Vault(
            shareId = ShareId("123"),
            name = "default",
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

