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

package proton.android.pass.payments.impl

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.android.payment.common.exception.PaymentException
import me.proton.android.payment.product.model.Offer
import me.proton.android.payment.product.model.Product
import me.proton.android.payment.product.usecase.GetProducts
import me.proton.android.payment.purchase.model.PendingPurchase
import me.proton.android.payment.purchase.model.SessionState
import me.proton.android.payment.purchase.usecase.ObserveSessionState
import me.proton.android.payment.purchase.usecase.PurchaseProduct
import proton.android.pass.payments.api.PaymentOffer
import proton.android.pass.payments.api.PaymentPricingPhase
import proton.android.pass.payments.api.PaymentProduct
import proton.android.pass.payments.api.PaymentSessionState
import proton.android.pass.payments.api.Payments
import proton.android.pass.payments.api.ReconciledPayment
import javax.inject.Inject

class PaymentsImpl @Inject constructor(
    private val getPaymentProducts: GetProducts,
    private val purchaseProduct: PurchaseProduct,
    private val observePaymentSessionState: ObserveSessionState
) : Payments {
    override val supportsStorePayments: Boolean = true

    override suspend fun getProducts(): Result<List<PaymentProduct>> = getPaymentProducts()
        .map { products -> products.map(Product::toPaymentProduct) }

    override fun observeSessionState(): Flow<PaymentSessionState> = observePaymentSessionState().map { state ->
        when (state) {
            is SessionState.Reconciling.Terminal.Success -> PaymentSessionState.Reconciled(
                ReconciledPayment(
                    planId = state.purchase.planId,
                    cycle = state.purchase.cycle,
                    orderId = state.purchase.orderId
                )
            )

            is SessionState.Purchasing.Terminal.Failure -> state.exception.toFailure()

            is SessionState.Reconciling.Terminal.Failure -> state.exception.toFailure()

            is SessionState.Idle -> PaymentSessionState.Idle
            else -> PaymentSessionState.Processing
        }
    }

    override suspend fun purchase(productId: String, offerToken: String): Result<Unit> =
        purchaseProduct(PendingPurchase(productId, offerToken)).map { }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class PaymentsModule {
    @Binds
    abstract fun bindPayments(impl: PaymentsImpl): Payments
}

private fun PaymentException.toFailure() = PaymentSessionState.Failure(
    isStoreError = this is PaymentException.StoreError
)

private fun Product.toPaymentProduct() = PaymentProduct(
    id = id,
    offers = offers.map { offer ->
        PaymentOffer(
            token = offer.token,
            isDiscounted = offer is Offer.Discounted,
            pricingPhases = offer.pricingPhases.map { phase ->
                PaymentPricingPhase(
                    amount = phase.price.amount,
                    currency = phase.price.currency,
                    formattedAmount = phase.price.formattedAmount,
                    period = phase.period
                )
            }
        )
    }
)
