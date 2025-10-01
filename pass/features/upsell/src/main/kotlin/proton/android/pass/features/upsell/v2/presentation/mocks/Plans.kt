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

package proton.android.pass.features.upsell.v2.presentation.mocks

import kotlinx.collections.immutable.persistentListOf
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonpresentation.api.plan.PaymentButtonUiState
import proton.android.pass.features.upsell.v2.models.SelectorUiState
import proton.android.pass.features.upsell.v2.models.UpsellPlanUiModel

internal val mockSelectors = persistentListOf(
    SelectorUiState("Plus", "CHF 2.99"),
    SelectorUiState("Unlimited", "CHF 9.99")
)
internal val mockAnnualPlans = persistentListOf(
    UpsellPlanUiModel(
        selector = mockSelectors[0],
        bottomAnnualPrice = "Plan annual",
        paymentButtonUiState = PaymentButtonUiState(
            currency = "EUR",
            cycle = 12,
            plan = null,
            userId = UserId("foo")
        )
    ),
    UpsellPlanUiModel(
        selector = mockSelectors[1],
        bottomAnnualPrice = "Plan annual",
        paymentButtonUiState = PaymentButtonUiState(
            currency = "EUR",
            cycle = 12,
            plan = null,
            userId = UserId("foo")
        )
    )
)

internal val mockWelcomeMonthlyPlan = persistentListOf(
    UpsellPlanUiModel(
        selector = mockSelectors[0],
        bottomAnnualPrice = "Plan monthly",
        paymentButtonUiState = PaymentButtonUiState(
            currency = "EUR",
            cycle = 12,
            plan = null,
            userId = UserId("foo")
        ),
        defaultPricePerMonth = "CHF 4.99"
    )
)

internal val mockWelcomeYearlyPlan = persistentListOf(
    UpsellPlanUiModel(
        selector = mockSelectors[0],
        bottomAnnualPrice = "Plan yearly",
        paymentButtonUiState = PaymentButtonUiState(
            currency = "EUR",
            cycle = 12,
            plan = null,
            userId = UserId("foo")
        ),
        pricePerYear = "CHF 23.88"
    )
)
