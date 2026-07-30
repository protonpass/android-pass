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

package proton.android.pass.account.fakes.payment

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.plan.domain.entity.Subscription
import me.proton.core.plan.domain.usecase.PerformSubscribe
import javax.inject.Inject

class FakePerformSubscribe @Inject constructor() : PerformSubscribe {

    var result: Subscription? = null
    var error: Throwable? = null
    var callCount: Int = 0

    override suspend fun invoke(
        cycle: SubscriptionCycle,
        paymentToken: ProtonPaymentToken?,
        planNames: List<String>,
        userId: UserId
    ): Subscription {
        callCount++
        error?.let { throw it }
        return requireNotNull(result) { "FakePerformSubscribe.result is not set" }
    }
}
