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
import kotlinx.coroutines.flow.filterNotNull
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
import proton.android.pass.domain.Plan
import proton.android.pass.domain.ShareSelection
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
    private val supportsPayments: Boolean = appConfig.flavor.supportPayment()

    override fun invoke(userId: UserId?): Flow<UpgradeInfo> = getUserIdFlow(userId)
        .flatMapLatest { id ->
            val isSubscriptionAvailable = resolveSubscriptionAvailability(id)

            combine(
                observePlan(id),
                observeTotpCount(),
                observeAliasCount(),
                observeVaultTotal(id)
            ) { plan, totalTotp, totalAlias, totalVaults ->
                UpgradeInfo(
                    isUpgradeAvailable = shouldDisplayUpgrade(plan),
                    isSubscriptionAvailable = isSubscriptionAvailable,
                    plan = plan,
                    totalVaults = totalVaults,
                    totalAlias = totalAlias,
                    totalTotp = totalTotp
                )
            }
        }

    private fun getUserIdFlow(userId: UserId?): Flow<UserId> = userId?.let(::flowOf)
        ?: observeCurrentUser()
            .map { it.userId }
            .distinctUntilChanged()

    private suspend fun resolveSubscriptionAvailability(userId: UserId): Boolean {
        if (!supportsPayments) return true
        return runCatching { paymentManager.isSubscriptionAvailable(userId) }.getOrDefault(false)
    }

    private fun observePlan(userId: UserId): Flow<Plan> = planRepository.observePlan(userId = userId)
        .filterNotNull()
        .distinctUntilChanged()

    private fun observeTotpCount(): Flow<Int> = observeMFACount(includeHiddenVault = true).distinctUntilChanged()

    private fun observeAliasCount(): Flow<Int> = observeItemCount(
        itemState = null,
        shareSelection = ShareSelection.AllShares,
        includeHiddenVault = true
    )
        .map { it.alias.toBoundedInt() }
        .distinctUntilChanged()

    private fun observeVaultTotal(userId: UserId): Flow<Int> =
        observeVaultCount(userId, includeHidden = true).distinctUntilChanged()

    private fun shouldDisplayUpgrade(plan: Plan): Boolean = !plan.hideUpgrade && !plan.isPaidPlan

    private fun Long.toBoundedInt(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
}
