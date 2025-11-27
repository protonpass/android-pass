package proton.android.pass.account.fakes.plan

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.entity.SubscriptionCycle
import me.proton.core.plan.domain.entity.Subscription
import me.proton.core.plan.domain.usecase.PerformSubscribe
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakePerformSubscribe @Inject constructor() : PerformSubscribe {
    override suspend fun invoke(
        cycle: SubscriptionCycle,
        paymentToken: ProtonPaymentToken?,
        planNames: List<String>,
        userId: UserId
    ): Subscription {
        TODO("Not yet implemented")
    }
}
