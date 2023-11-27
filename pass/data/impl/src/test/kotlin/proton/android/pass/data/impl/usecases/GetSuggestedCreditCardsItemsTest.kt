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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.SuggestedCreditCardItemsResult
import proton.android.pass.data.fakes.usecases.TestGetUserPlan
import proton.android.pass.data.fakes.usecases.TestObserveActiveItems
import proton.android.pass.data.fakes.usecases.TestObserveItems
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.domain.PlanType

class GetSuggestedCreditCardsItemsTest {

    private lateinit var instance: GetSuggestedCreditCardItemsImpl

    private lateinit var getUserPlan: TestGetUserPlan
    private lateinit var observeActiveItems: TestObserveActiveItems

    @Before
    fun setup() {
        getUserPlan = TestGetUserPlan()
        observeActiveItems = TestObserveActiveItems()
        instance = GetSuggestedCreditCardItemsImpl(
            getUserPlan = getUserPlan,
            observeActiveItems = observeActiveItems,
        )
    }

    @Test
    fun `when plan is free and no credit cards, then hide`() = runTest {
        getUserPlan.setResult(Result.success(buildPlan(PlanType.Free("", ""))))
        observeActiveItems.sendItemList(emptyList())

        val result = instance().first()
        assertThat(result).isInstanceOf(SuggestedCreditCardItemsResult.Hide::class.java)
    }

    @Test
    fun `when plan is free and credit cards, then show upgrade`() = runTest {
        getUserPlan.setResult(Result.success(buildPlan(PlanType.Free("", ""))))
        observeActiveItems.sendItemList(listOf(buildItem()))

        val result = instance().first()
        assertThat(result).isInstanceOf(SuggestedCreditCardItemsResult.ShowUpgrade::class.java)
    }

    @Test
    fun `when plan is paid and no credit cards, show empty list`() = runTest {
        getUserPlan.setResult(Result.success(buildPlan(PlanType.Paid("", ""))))
        observeActiveItems.sendItemList(emptyList())

        val result = instance().first()
        assertThat(result).isInstanceOf(SuggestedCreditCardItemsResult.Items::class.java)

        val items = (result as SuggestedCreditCardItemsResult.Items).items
        assertThat(items).isEmpty()
    }

    @Test
    fun `when plan is paid and credit cards, show credit cards sorted by last used`() = runTest {
        getUserPlan.setResult(Result.success(buildPlan(PlanType.Paid("", ""))))

        val item1 = buildItem()
        val item2 = buildItem()
        val expectedItems = listOf(item1, item2)
        observeActiveItems.sendItemList(expectedItems)

        val result = instance().first()
        assertThat(result).isInstanceOf(SuggestedCreditCardItemsResult.Items::class.java)

        val items = (result as SuggestedCreditCardItemsResult.Items).items
        assertThat(items).isEqualTo(expectedItems.reversed()) // Must be reversed
    }

    private fun buildItem() = TestObserveItems.createCreditCard()

    private fun buildPlan(planType: PlanType) = Plan(
        planType = planType,
        hideUpgrade = false,
        vaultLimit = PlanLimit.Unlimited,
        aliasLimit = PlanLimit.Unlimited,
        totpLimit = PlanLimit.Unlimited,
        updatedAt = 0
    )
}
