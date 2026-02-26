/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.account.fakes.plan

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.payment.domain.entity.Currency
import me.proton.core.payment.domain.entity.PaymentTokenEntity
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.payment.domain.entity.SubscriptionStatus
import me.proton.core.payment.domain.repository.PlanQuantity
import me.proton.core.plan.domain.entity.DynamicPlans
import me.proton.core.plan.domain.entity.DynamicSubscription
import me.proton.core.plan.domain.entity.Subscription
import me.proton.core.plan.domain.repository.PlansRepository
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakePlansRepository @Inject constructor() : PlansRepository {
    override suspend fun getDynamicPlans(sessionUserId: SessionUserId?, appStore: AppStore): DynamicPlans {
        TODO("Not yet implemented")
    }

    override suspend fun validateSubscription(
        sessionUserId: SessionUserId?,
        codes: List<String>?,
        plans: PlanQuantity,
        currency: Currency,
        cycle: SubscriptionCycle
    ): SubscriptionStatus {
        TODO("Not yet implemented")
    }

    override suspend fun getSubscription(sessionUserId: SessionUserId): Subscription? {
        TODO("Not yet implemented")
    }

    override suspend fun getDynamicSubscriptions(sessionUserId: SessionUserId): List<DynamicSubscription> {
        TODO("Not yet implemented")
    }

    override suspend fun createOrUpdateSubscription(
        sessionUserId: SessionUserId,
        payment: PaymentTokenEntity?,
        plans: PlanQuantity,
        cycle: SubscriptionCycle
    ): Subscription {
        TODO("Not yet implemented")
    }
}
