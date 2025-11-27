package proton.android.pass.account.fakes.payment

import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.usecase.FindGooglePurchaseForPaymentOrderId
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakeFindGooglePurchaseForPaymentOrderId @Inject constructor() : FindGooglePurchaseForPaymentOrderId {
    override suspend fun invoke(orderId: String?): GooglePurchase? {
        TODO("Not yet implemented")
    }
}
