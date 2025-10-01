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

package proton.android.pass.data.impl.usecases.plan

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getPrimaryAccount
import me.proton.core.domain.entity.UserId
import me.proton.core.plan.domain.entity.DynamicPlan
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.entity.filterBy
import me.proton.core.plan.domain.usecase.GetDynamicPlansAdjustedPrices
import me.proton.core.plan.domain.usecase.ObserveUserCurrency
import me.proton.core.plan.presentation.usecase.ComposeAutoRenewText
import me.proton.core.presentation.utils.formatCentsPriceDefaultLocale
import proton.android.pass.common.api.AppDispatchers
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.plan.ANNUAL_PLAN_CYCLE
import proton.android.pass.data.api.usecases.plan.MONTHLY_PLAN_CYCLE
import proton.android.pass.data.api.usecases.plan.ObservePlansWithPrice
import proton.android.pass.data.api.usecases.plan.PASS_PLUS_NAME
import proton.android.pass.data.api.usecases.plan.PASS_UNLIMITED_NAME
import proton.android.pass.domain.plan.OnePlanWithPrice
import proton.android.pass.domain.plan.PaymentButton
import proton.android.pass.domain.plan.PlanWithPriceState
import javax.inject.Inject

class ObservePlansWithPriceImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val observeUpgradeInfo: ObserveUpgradeInfo,
    private val getDynamicPlans: GetDynamicPlansAdjustedPrices,
    private val autoRenewText: ComposeAutoRenewText,
    private val observeUserCurrency: ObserveUserCurrency,
    private val appDispatchers: AppDispatchers
) : ObservePlansWithPrice {


    override fun invoke(): Flow<PlanWithPriceState> = flow {
        accountManager
            .getPrimaryAccount()
            .filterNotNull()
            .flatMapLatest { account ->
                observeUserCurrency(account.userId).map {
                    Pair(account, it)
                }
            }
            .flatMapLatest { (account, currency) ->
                observeUpgradeInfo(account.userId).asLoadingResult().map {
                    Triple(account, currency, it)
                }
            }
            .collect { (account, userCurrency, upgradeInfo) ->

                when (upgradeInfo) {
                    LoadingResult.Loading -> {
                        emit(PlanWithPriceState.Loading)
                    }

                    is LoadingResult.Error -> {
                        emit(PlanWithPriceState.Error)
                    }

                    is LoadingResult.Success -> {
                        val canShow =
                            upgradeInfo.data.plan.isFreePlan && upgradeInfo.data.isUpgradeAvailable
                        if (canShow) {
                            runCatching {
                                getDynamicPlans(userId = account.userId)
                            }.onSuccess { prices ->
                                if (prices.plans.isEmpty()) {
                                    emit(PlanWithPriceState.NoPlan)
                                } else {
                                    emit(
                                        managePlans(
                                            prices = prices,
                                            userCurrency = userCurrency,
                                            userId = account.userId
                                        )
                                    )
                                }
                            }.onFailure {
                                emit(PlanWithPriceState.Error)
                            }

                        } else {
                            emit(PlanWithPriceState.NoPlan)
                        }
                    }

                }
            }
    }
        .onStart { emit(PlanWithPriceState.Loading) }
        .flowOn(appDispatchers.io)


    private fun managePlans(
        prices: DynamicPlans,
        userCurrency: String,
        userId: UserId
    ): PlanWithPriceState {

        val monthlyPlans =
            getPlanByCycle(
                prices = prices,
                userCurrency = userCurrency,
                cycle = MONTHLY_PLAN_CYCLE,
                userId = userId
            )

        val annualPlans =
            getPlanByCycle(
                prices = prices,
                userCurrency = userCurrency,
                cycle = ANNUAL_PLAN_CYCLE,
                userId = userId
            )

        if (annualPlans.isEmpty() && monthlyPlans.isEmpty()) {
            return PlanWithPriceState.NoPlan
        }

        return PlanWithPriceState.PlansAvailable(
            monthlyPlans = monthlyPlans,
            annualPlans = annualPlans
        )
    }

    private fun getPlanByCycle(
        prices: DynamicPlans,
        userId: UserId,
        userCurrency: String,
        cycle: Int
    ): List<OnePlanWithPrice> {
        val plans = mutableListOf<OnePlanWithPrice>()

        var currencyDisplayed = userCurrency

        val filtered = prices.plans
            .filterBy(cycle = cycle, currency = currencyDisplayed)

        val effectiveList = filtered.ifEmpty {
            currencyDisplayed = DEFAULT_CURRENCY
            prices.plans.filterBy(cycle = cycle, currency = currencyDisplayed)
        }

        val orderedPlans =
            effectiveList
                .sortedBy {
                    when (it.name) {
                        PASS_PLUS_NAME -> 0
                        PASS_UNLIMITED_NAME -> 1
                        else -> 2
                    }
                }
                .take(n = 2)

        orderedPlans.forEach {
            plans.add(
                OnePlanWithPrice(
                    internalName = it.name.orEmpty(),
                    title = when (it.name) {
                        PASS_PLUS_NAME -> "Plus"
                        PASS_UNLIMITED_NAME -> "Unlimited"
                        else -> ""
                    },
                    pricePerMonth = it.getMonthlyPrice(
                        currency = currencyDisplayed,
                        cycle = cycle
                    ).orEmpty(),
                    defaultPricePerMonth = it.getDefaultMonthlyPrice(
                        currency = currencyDisplayed,
                        cycle = cycle
                    ),
                    pricePerYear = it.getYearlyPrice(
                        currency = currencyDisplayed,
                        cycle = cycle
                    ).orEmpty(),
                    annualPrice = autoRenewText(
                        price = it.instances[cycle]?.price?.values
                            ?.firstOrNull { it.currency == currencyDisplayed },
                        cycle = cycle
                    ).orEmpty(),
                    paymentInfo = PaymentButton(
                        currency = currencyDisplayed,
                        cycle = cycle,
                        plan = it,
                        userId = userId
                    ),
                    cycle = cycle
                )
            )
        }

        return plans
    }

    private fun DynamicPlan.getYearlyPrice(currency: String, cycle: Int): String? = instances[cycle]
        ?.price
        ?.values
        ?.firstOrNull { it.currency == currency }
        ?.current
        ?.toDouble()
        ?.formatCentsPriceDefaultLocale(currency)

    private fun DynamicPlan.getMonthlyPrice(currency: String, cycle: Int): String? = instances[cycle]
        ?.price
        ?.values
        ?.firstOrNull { it.currency == currency }
        ?.current
        ?.div(other = cycle.toFloat())
        ?.toDouble()
        ?.formatCentsPriceDefaultLocale(currency)

    private fun DynamicPlan.getDefaultMonthlyPrice(currency: String, cycle: Int): String? = instances[cycle]
        ?.price
        ?.values
        ?.firstOrNull { it.currency == currency }
        ?.default
        ?.div(other = cycle.toFloat())
        ?.toDouble()
        ?.formatCentsPriceDefaultLocale(currency)
}

private const val DEFAULT_CURRENCY = "USD"
