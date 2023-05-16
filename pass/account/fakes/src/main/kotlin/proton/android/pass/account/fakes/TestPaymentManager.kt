package proton.android.pass.account.fakes

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.PaymentManager
import me.proton.core.payment.domain.usecase.PaymentProvider
import javax.inject.Inject

class TestPaymentManager @Inject constructor() : PaymentManager {

    var paymentProviders: Set<PaymentProvider> = emptySet()
    var isUpgradeAvailable: Boolean = true
    var isSubscriptionAvailable: Boolean = true

    override suspend fun getPaymentProviders(
        userId: UserId?,
        refresh: Boolean
    ): Set<PaymentProvider> = paymentProviders

    override suspend fun isUpgradeAvailable(
        userId: UserId?,
        refresh: Boolean
    ): Boolean = isUpgradeAvailable

    override suspend fun isSubscriptionAvailable(
        userId: UserId,
        refresh: Boolean
    ): Boolean = isSubscriptionAvailable
}
