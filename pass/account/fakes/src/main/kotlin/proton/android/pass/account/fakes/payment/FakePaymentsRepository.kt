package proton.android.pass.account.fakes.payment

import me.proton.core.domain.entity.AppStore
import me.proton.core.domain.entity.SessionUserId
import me.proton.core.payment.domain.entity.PaymentMethod
import me.proton.core.payment.domain.entity.PaymentStatus
import me.proton.core.payment.domain.entity.PaymentTokenResult
import me.proton.core.payment.domain.entity.PaymentType
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.repository.PaymentsRepository
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakePaymentsRepository @Inject constructor() : PaymentsRepository {
    override suspend fun createOmnichannelPaymentToken(
        sessionUserId: SessionUserId?,
        packageName: String,
        productId: String,
        orderId: String
    ): PaymentTokenResult.CreatePaymentTokenResult {
        TODO("Not yet implemented")
    }

    override suspend fun createPaymentToken(
        sessionUserId: SessionUserId?,
        paymentType: PaymentType
    ): PaymentTokenResult.CreatePaymentTokenResult {
        TODO("Not yet implemented")
    }

    override suspend fun getPaymentTokenStatus(
        sessionUserId: SessionUserId?,
        paymentToken: ProtonPaymentToken
    ): PaymentTokenResult.PaymentTokenStatusResult {
        TODO("Not yet implemented")
    }

    override suspend fun getAvailablePaymentMethods(sessionUserId: SessionUserId): List<PaymentMethod> {
        TODO("Not yet implemented")
    }

    override suspend fun getPaymentStatus(sessionUserId: SessionUserId?, appStore: AppStore): PaymentStatus {
        TODO("Not yet implemented")
    }

}
