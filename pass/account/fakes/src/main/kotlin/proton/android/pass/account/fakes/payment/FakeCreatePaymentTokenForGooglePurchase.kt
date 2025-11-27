package proton.android.pass.account.fakes.payment

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.entity.GooglePurchase
import me.proton.core.payment.domain.entity.ProductId
import me.proton.core.payment.domain.usecase.CreatePaymentTokenForGooglePurchase
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakeCreatePaymentTokenForGooglePurchase @Inject constructor() : CreatePaymentTokenForGooglePurchase {
    override suspend fun invoke(
        googleProductId: ProductId,
        purchase: GooglePurchase,
        userId: UserId?
    ): CreatePaymentTokenForGooglePurchase.Result {
        TODO("Not yet implemented")
    }
}
