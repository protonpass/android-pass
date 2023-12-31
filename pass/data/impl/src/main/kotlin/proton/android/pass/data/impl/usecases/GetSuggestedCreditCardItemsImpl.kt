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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.data.api.usecases.GetSuggestedCreditCardItems
import proton.android.pass.data.api.usecases.GetUserPlan
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveActiveItems
import proton.android.pass.data.api.usecases.SuggestedCreditCardItemsResult
import proton.android.pass.domain.PlanType
import javax.inject.Inject

class GetSuggestedCreditCardItemsImpl @Inject constructor(
    private val observeActiveItems: ObserveActiveItems,
    private val getUserPlan: GetUserPlan
) : GetSuggestedCreditCardItems {

    override fun invoke(): Flow<SuggestedCreditCardItemsResult> = combine(
        getUserPlan(),
        observeActiveItems(filter = ItemTypeFilter.CreditCards)
    ) { plan, creditCards ->
        when (plan.planType) {
            is PlanType.Free -> if (creditCards.isEmpty()) {
                SuggestedCreditCardItemsResult.Hide
            } else {
                SuggestedCreditCardItemsResult.ShowUpgrade
            }
            is PlanType.Paid,
            is PlanType.Trial -> {
                val sorted = creditCards.sortedByDescending {
                    when (val autofillTime = it.lastAutofillTime) {
                        None -> it.modificationTime
                        is Some -> autofillTime.value
                    }
                }
                SuggestedCreditCardItemsResult.Items(sorted)
            }
            is PlanType.Unknown -> SuggestedCreditCardItemsResult.Hide
        }
    }
}
