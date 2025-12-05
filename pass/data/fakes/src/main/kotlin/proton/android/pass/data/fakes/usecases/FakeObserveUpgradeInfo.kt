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

package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.domain.Plan
import proton.android.pass.domain.PlanLimit
import proton.android.pass.test.TestConstants
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveUpgradeInfo @Inject constructor() : ObserveUpgradeInfo {

    private var upgradeInfo: MutableSharedFlow<UpgradeInfo> = MutableStateFlow(DEFAULT)

    fun setResult(value: UpgradeInfo) {
        upgradeInfo.tryEmit(value)
    }

    override fun invoke(userId: UserId?): Flow<UpgradeInfo> = upgradeInfo

    companion object {
        val DEFAULT = UpgradeInfo(
            isSubscriptionAvailable = false,
            isUpgradeAvailable = false,
            plan = Plan(
                planType = TestConstants.FreePlanType,
                vaultLimit = PlanLimit.Limited(0),
                aliasLimit = PlanLimit.Limited(0),
                totpLimit = PlanLimit.Limited(0),
                updatedAt = 0,
                hideUpgrade = false
            ),
            totalVaults = 0,
            totalAlias = 0,
            totalTotp = 0
        )
    }
}
