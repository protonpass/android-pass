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

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.accountmanager.domain.getAccounts
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.RefreshUserAccess
import proton.android.pass.data.impl.R
import proton.android.pass.data.impl.repositories.PlanRepository
import proton.android.pass.domain.Plan
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import javax.inject.Inject

class RefreshUserAccessImpl @Inject constructor(
    private val planRepository: PlanRepository,
    private val accountManager: AccountManager,
    private val toastManager: ToastManager
) : RefreshUserAccess {

    override suspend fun invoke(userId: UserId) {
        PassLogger.i(TAG, "Refreshing plan for $userId")
        val oldPlan: Plan? = planRepository.observePlan(userId).firstOrNull()
        planRepository.refreshPlan(userId)
        val newPlan: Plan = planRepository.observePlan(userId).filterNotNull().first()
        val isOldPlanPaid = oldPlan?.isPaidPlan ?: false
        if (isOldPlanPaid && newPlan.isFreePlan) {
            val list = getAllActivePlans()
            disableAllFreeAccountsButOne(list)
        }
    }

    private suspend fun getAllActivePlans(): List<Pair<UserId, Plan>> = accountManager.getAccounts(AccountState.Ready)
        .flatMapLatest { list ->
            val planFlows = list.map { account ->
                planRepository.observePlan(account.userId)
                    .filterNotNull()
                    .map { plan -> account.userId to plan }
            }
            combine<Pair<UserId, Plan>, List<Pair<UserId, Plan>>>(
                planFlows,
                Array<Pair<UserId, Plan>>::toList
            )
        }
        .firstOrNull()
        ?: emptyList()


    private suspend fun disableAllFreeAccountsButOne(list: List<Pair<UserId, Plan>>) {
        val freePlansToDisable = list.filter { (_, plan) -> plan.isFreePlan }.drop(1)
        if (freePlansToDisable.isNotEmpty()) {
            toastManager.showToast(R.string.logging_out_free_users)
            freePlansToDisable.map { (userId, _) ->
                PassLogger.i(TAG, "Disabling free account: $userId")
                accountManager.disableAccount(userId)
            }
        }
    }

    companion object {
        private const val TAG = "RefreshPlanImpl"
    }
}
