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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.PaymentManager
import proton.android.pass.appconfig.api.AppConfig
import proton.android.pass.appconfig.api.BuildFlavor.Companion.supportPayment
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultCount
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.impl.repositories.PlanRepository
import javax.inject.Inject

class ObserveUpgradeInfoImpl @Inject constructor(
    private val appConfig: AppConfig,
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeMFACount: ObserveMFACount,
    private val observeItemCount: ObserveItemCount,
    private val paymentManager: PaymentManager,
    private val planRepository: PlanRepository,
    private val observeVaultCount: ObserveVaultCount
) : ObserveUpgradeInfo {
    override fun invoke(userId: UserId?, forceRefresh: Boolean): Flow<UpgradeInfo> =
        (userId?.let(::flowOf) ?: observeCurrentUser().map { it.userId })
            .flatMapLatest { id ->
                val supportsPayments = appConfig.flavor.supportPayment()
                val (isSubscriptionAvailable, isUpgradeAvailable) = if (supportsPayments) {
                    paymentManager.isSubscriptionAvailable(id) to paymentManager.isUpgradeAvailable()
                } else {
                    true to true
                }

                combine(
                    planRepository.observePlan(
                        userId = id,
                        forceRefresh = forceRefresh
                    ),
                    observeMFACount(),
                    observeItemCount(itemState = null),
                    observeVaultCount(id)
                ) { plan, mfaCount, itemCount, vaultCount ->
                    val displayUpgrade = when {
                        plan.hideUpgrade -> false
                        else -> isUpgradeAvailable && !plan.isPaidPlan
                    }
                    UpgradeInfo(
                        isUpgradeAvailable = displayUpgrade,
                        isSubscriptionAvailable = isSubscriptionAvailable,
                        plan = plan,
                        totalVaults = vaultCount,
                        totalAlias = itemCount.alias.toInt(),
                        totalTotp = mfaCount
                    )
                }
            }
            .distinctUntilChanged()
}
