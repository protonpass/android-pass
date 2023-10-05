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
import me.proton.core.payment.domain.PaymentManager
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveItemCount
import proton.android.pass.data.api.usecases.ObserveMFACount
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultCount
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.impl.repositories.PlanRepository
import proton.pass.domain.PlanType
import javax.inject.Inject

class ObserveUpgradeInfoImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val observeMFACount: ObserveMFACount,
    private val observeItemCount: ObserveItemCount,
    private val paymentManager: PaymentManager,
    private val planRepository: PlanRepository,
    private val observeVaultCount: ObserveVaultCount
) : ObserveUpgradeInfo {
    override fun invoke(forceRefresh: Boolean): Flow<UpgradeInfo> = observeCurrentUser()
        .distinctUntilChanged()
        .flatMapLatest { user ->
            val isSubscriptionAvailable = paymentManager.isSubscriptionAvailable(user.userId)
            val isUpgradeAvailable = paymentManager.isUpgradeAvailable()
            combine(
                planRepository.sendUserAccessAndObservePlan(
                    userId = user.userId,
                    forceRefresh = forceRefresh
                ),
                observeMFACount(),
                observeItemCount(itemState = null),
                observeVaultCount(user.userId)
            ) { plan, mfaCount, itemCount, vaultCount ->
                val isPaid = plan.planType is PlanType.Paid
                val displayUpgrade = when {
                    plan.hideUpgrade -> false
                    else -> isUpgradeAvailable && !isPaid
                }
                UpgradeInfo(
                    isUpgradeAvailable = displayUpgrade,
                    isSubscriptionAvailable = isSubscriptionAvailable,
                    plan = plan.copy(
                        vaultLimit = plan.vaultLimit,
                        aliasLimit = plan.aliasLimit,
                        totpLimit = plan.totpLimit,
                    ),
                    totalVaults = vaultCount,
                    totalAlias = itemCount.alias.toInt(),
                    totalTotp = mfaCount
                )
            }
        }
        .distinctUntilChanged()
}
