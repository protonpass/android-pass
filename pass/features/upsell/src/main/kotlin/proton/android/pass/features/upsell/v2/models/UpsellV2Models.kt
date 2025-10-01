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

package proton.android.pass.features.upsell.v2.models

import android.content.Context
import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.commonpresentation.api.plan.PaymentButtonUiState
import proton.android.pass.commonpresentation.api.plan.toUiModel
import proton.android.pass.data.api.usecases.plan.PASS_PLUS_NAME
import proton.android.pass.domain.plan.OnePlanWithPrice
import proton.android.pass.domain.plan.PlanWithPriceState
import proton.android.pass.features.upsell.v1.R

data class SelectorUiState(
    val title: String = "",
    val pricePerMonth: String = ""
)

data class UpsellPlanUiModel(
    val selector: SelectorUiState = SelectorUiState(),
    val defaultPricePerMonth: String? = null,
    val pricePerYear: String = "",
    val paymentButtonUiState: PaymentButtonUiState,
    val bottomAnnualPrice: String = "",
    // items to display
    val description: ImmutableList<UpsellItemsUiState> = persistentListOf()
)

fun PlanWithPriceState.PlansAvailable.filterWelcomeOfferMonthly(): OnePlanWithPrice? = this.monthlyPlans
    .firstOrNull {
        it.defaultPricePerMonth != null && it.internalName == PASS_PLUS_NAME
    }

fun PlanWithPriceState.PlansAvailable.filterWelcomeOfferYearly(): OnePlanWithPrice? = this.annualPlans
    .firstOrNull {
        it.defaultPricePerMonth != null && it.internalName == PASS_PLUS_NAME
    }


fun PlanWithPriceState.PlansAvailable.toWelcomeOfferMonthlyUpsellUiModel(context: Context): UpsellPlanUiModel? =
    this.filterWelcomeOfferMonthly()?.let {
        UpsellPlanUiModel(
            selector = SelectorUiState(
                it.title,
                it.pricePerMonth
            ),
            defaultPricePerMonth = it.defaultPricePerMonth,
            pricePerYear = it.pricePerYear,
            paymentButtonUiState = it.paymentInfo.toUiModel().copy(
                defaultButtonText = context.getString(
                    R.string.upsell_get_one_month,
                    it.pricePerMonth
                )
            ),
            bottomAnnualPrice = it.annualPrice
        )
    }


fun PlanWithPriceState.PlansAvailable.toWelcomeOfferYearlyUpsellUiModel(context: Context): UpsellPlanUiModel? =
    this.filterWelcomeOfferYearly()?.let {
        UpsellPlanUiModel(
            selector = SelectorUiState(
                it.title,
                it.pricePerMonth
            ),
            defaultPricePerMonth = it.defaultPricePerMonth,
            pricePerYear = it.pricePerYear,
            paymentButtonUiState = it.paymentInfo.toUiModel().copy(
                defaultButtonText = context.getString(
                    R.string.upsell_get_the_deal
                )
            ),
            bottomAnnualPrice = it.annualPrice
        )
    }

fun PlanWithPriceState.PlansAvailable.toYearlyUpsellUiModel(): List<UpsellPlanUiModel> = this.annualPlans
    .map {
        UpsellPlanUiModel(
            selector = SelectorUiState(
                it.title,
                it.pricePerMonth
            ),
            defaultPricePerMonth = it.defaultPricePerMonth,
            pricePerYear = it.pricePerYear,
            paymentButtonUiState = it.paymentInfo.toUiModel(),
            bottomAnnualPrice = it.annualPrice
        )
    }
    .take(n = 2)


sealed class PlanTypeUiState {
    class TextRes(val id: Int) : PlanTypeUiState()
    class Text(val text: String) : PlanTypeUiState()

    object Check : PlanTypeUiState()
    object Empty : PlanTypeUiState()
}

@Stable
data class UpsellItemsUiState(
    val title: Int,
    val from: PlanTypeUiState,
    val to: PlanTypeUiState
)

val elementsPlusPlan = persistentListOf(
    UpsellItemsUiState(
        R.string.upsell_plan_plus_1,
        PlanTypeUiState.Text(text = "10"),
        PlanTypeUiState.Text(text = "∞")
    ),
    UpsellItemsUiState(
        R.string.upsell_plan_plus_2,
        PlanTypeUiState.Empty,
        PlanTypeUiState.Check
    ),
    UpsellItemsUiState(
        R.string.upsell_plan_plus_3,
        PlanTypeUiState.Empty,
        PlanTypeUiState.Check
    ),
    UpsellItemsUiState(
        R.string.upsell_plan_plus_4,
        PlanTypeUiState.Empty,
        PlanTypeUiState.Text(text = "∞")
    ),
    UpsellItemsUiState(
        R.string.upsell_plan_plus_5,
        PlanTypeUiState.Empty,
        PlanTypeUiState.Check
    ),
    UpsellItemsUiState(
        R.string.upsell_plan_plus_6,
        PlanTypeUiState.Empty,
        PlanTypeUiState.Check
    )
)

val elementsUnlimitedPlan = persistentListOf(
    UpsellItemsUiState(
        R.string.upsell_plan_unlimited_1,
        PlanTypeUiState.Text(text = "10 GB"),
        PlanTypeUiState.Text(text = "500 GB")
    ),
    UpsellItemsUiState(
        R.string.upsell_plan_unlimited_2,
        PlanTypeUiState.Text(text = "1"),
        PlanTypeUiState.Text(text = "15")
    ),
    UpsellItemsUiState(
        R.string.upsell_plan_unlimited_3,
        PlanTypeUiState.Text(text = "1"),
        PlanTypeUiState.Text(text = "15")
    ),
    UpsellItemsUiState(
        R.string.upsell_plan_unlimited_4,
        PlanTypeUiState.TextRes(id = R.string.upsell_plan_unlimited_4_col1),
        PlanTypeUiState.TextRes(id = R.string.upsell_plan_unlimited_4_col2)
    )
)

enum class StepToDisplay {
    Idle, Loading, NoPlans,
    AnnualPlans, WelcomeOfferMonthly, WelcomeOfferYearly,
    Next
}

@Stable
data class UpsellV2UiState(
    val displayOnBoarding: Boolean = false,
    val stepToDisplay: StepToDisplay = StepToDisplay.Idle,
    val plans: ImmutableList<UpsellPlanUiModel> = persistentListOf()
)
