/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.domain.plan

import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.entity.DynamicPlan

data class PaymentButton(
    val currency: String = "",
    val cycle: Int = 1,
    val plan: DynamicPlan? = null,
    val userId: UserId? = null
)

data class OnePlanWithPrice(
    val internalName: String,
    val title: String,
    val pricePerYear: String,
    val pricePerMonth: String,
    val defaultPricePerMonth: String?, // if null -> no welcome offer
    val cycle: Int,
    val annualPrice: String,
    val paymentInfo: PaymentButton
)

sealed class PlanWithPriceState {
    data object Loading : PlanWithPriceState()
    data object Error : PlanWithPriceState()
    data object NoPlan : PlanWithPriceState()
    data class PlansAvailable(
        val monthlyPlans: List<OnePlanWithPrice>,
        val annualPlans: List<OnePlanWithPrice>
    ) : PlanWithPriceState()
}
