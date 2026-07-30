/*
 * Copyright (c) 2025-2026 Proton AG
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

package proton.android.pass.data.impl.usecases.plan

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.data.impl.R
import proton.android.pass.data.api.usecases.plan.ANNUAL_PLAN_CYCLE
import proton.android.pass.data.api.usecases.plan.MONTHLY_PLAN_CYCLE
import proton.android.pass.data.api.usecases.plan.ObservePlansWithPrice
import proton.android.pass.data.api.usecases.plan.PASS_PLUS_NAME
import proton.android.pass.data.api.usecases.plan.PASS_UNLIMITED_NAME
import proton.android.pass.domain.plan.OnePlanWithPrice
import proton.android.pass.domain.plan.PaymentButton
import proton.android.pass.domain.plan.PlanWithPriceState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.payments.api.PaymentPricingPhase
import proton.android.pass.payments.api.PaymentProduct
import proton.android.pass.payments.api.Payments
import javax.inject.Inject

class ObservePlansWithPriceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val payments: Payments,
    private val appDispatchers: AppDispatchers
) : ObservePlansWithPrice {

    override fun invoke(): Flow<PlanWithPriceState> = flow {
        payments.getProducts().onSuccess { products ->
            if (products.isEmpty()) {
                PassLogger.w(TAG, "Payments returned an empty product list")
                emit(PlanWithPriceState.NoPlan)
            } else {
                emit(managePlans(products))
            }
        }.onFailure { t ->
            PassLogger.w(TAG, "Failed to get payment products: $t")
            emit(PlanWithPriceState.Error)
        }
    }.onStart { emit(PlanWithPriceState.Loading) }
        .flowOn(appDispatchers.io)

    private fun managePlans(products: List<PaymentProduct>): PlanWithPriceState {
        val monthlyPlans = products.toOnePlanList(MONTHLY_PLAN_CYCLE)
        val annualPlans = products.toOnePlanList(ANNUAL_PLAN_CYCLE)

        if (annualPlans.isEmpty() && monthlyPlans.isEmpty()) {
            PassLogger.i(TAG, "managePlans: no plans available")
            return PlanWithPriceState.NoPlan
        }

        return PlanWithPriceState.PlansAvailable(
            monthlyPlans = monthlyPlans,
            annualPlans = annualPlans
        )
    }

    private fun List<PaymentProduct>.toOnePlanList(targetCycle: Int): List<OnePlanWithPrice> = this
        .filter { product ->
            val baseOffer = product.offers.firstOrNull { !it.isDiscounted }
                ?: return@filter false
            val period = baseOffer.pricingPhases.lastOrNull()?.period ?: return@filter false
            period == targetCycle.toPeriod() &&
                (product.id.contains(PASS_PLUS_NAME) || product.id.contains(PASS_UNLIMITED_NAME))
        }
        .sortedBy { product ->
            when {
                product.id.contains(PASS_PLUS_NAME) -> 0
                product.id.contains(PASS_UNLIMITED_NAME) -> 1
                else -> 2
            }
        }
        .mapNotNull { it.toOnePlanWithPrice(targetCycle) }

    private fun PaymentProduct.toOnePlanWithPrice(cycle: Int): OnePlanWithPrice? {
        val introOffer = offers.firstOrNull { it.isDiscounted }
        val baseOffer = offers.firstOrNull { !it.isDiscounted } ?: return null
        val phases = introOffer?.pricingPhases ?: baseOffer.pricingPhases
        val currentPhase = phases.firstOrNull() ?: return null
        val recurringPhase = phases.last()
        val currency = currentPhase.currency
        val hasOffer = introOffer != null

        return OnePlanWithPrice(
            internalName = when {
                id.contains(PASS_PLUS_NAME) -> PASS_PLUS_NAME
                id.contains(PASS_UNLIMITED_NAME) -> PASS_UNLIMITED_NAME
                else -> id
            },
            title = when {
                id.contains(PASS_PLUS_NAME) -> "Plus"
                id.contains(PASS_UNLIMITED_NAME) -> "Unlimited"
                else -> ""
            },
            pricePerMonth = currentPhase.toMonthlyFormatted(cycle, currency),
            defaultPricePerMonth = if (hasOffer) {
                recurringPhase.toMonthlyFormatted(cycle, currency)
            } else null,
            pricePerYear = currentPhase.formattedAmount,
            annualPrice = context.getString(
                when {
                    hasOffer && cycle == ANNUAL_PLAN_CYCLE -> R.string.plan_welcome_offer_auto_renews_annual
                    hasOffer -> R.string.plan_welcome_offer_auto_renews_monthly
                    cycle == ANNUAL_PLAN_CYCLE -> R.string.plan_subscription_auto_renews_annual
                    else -> R.string.plan_subscription_auto_renews_monthly
                },
                recurringPhase.formattedAmount
            ),
            paymentInfo = PaymentButton(
                productId = id,
                offerToken = introOffer?.token ?: baseOffer.token,
                formattedPrice = currentPhase.formattedAmount,
                currency = currency,
                rawPrice = currentPhase.amount.toDouble() / MICROS_PER_UNIT
            ),
            cycle = cycle
        )
    }
}

// Money.amount is in micros (1,000,000 micros = 1 currency unit = 100 cents).
// formatCentsPriceDefaultLocale expects a Double in cents.
private fun PaymentPricingPhase.toMonthlyFormatted(cycle: Int, currency: String): String =
    if (cycle == MONTHLY_PLAN_CYCLE) {
        formattedAmount
    } else {
        (amount.toDouble() / cycle / MICROS_PER_CENT).formatCentsPriceDefaultLocale(currency)
    }

private fun Int.toPeriod(): String = when (this) {
    MONTHLY_PLAN_CYCLE -> "P1M"
    ANNUAL_PLAN_CYCLE -> "P1Y"
    else -> ""
}

private const val TAG = "ObservePlansWithPrice"
private const val MICROS_PER_CENT = 10_000.0
private const val MICROS_PER_UNIT = 1_000_000.0
