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

package proton.android.pass.uitest.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import me.proton.android.payment.di.ScreenUseCaseModule
import me.proton.android.payment.product.usecase.GetProducts
import me.proton.android.payment.purchase.usecase.ObserveSessionState
import me.proton.android.payment.purchase.usecase.PurchaseProduct
import me.proton.android.payment.subscription.usecase.GetSubscriptions
import proton.android.pass.payments.api.PaymentOffer
import proton.android.pass.payments.api.PaymentPricingPhase
import proton.android.pass.payments.api.PaymentProduct
import proton.android.pass.payments.api.Payments
import proton.android.pass.payments.fakes.FakePayments
import proton.android.pass.payments.impl.PaymentsModule
import proton.android.pass.uitest.fakes.FakeGetProducts
import proton.android.pass.uitest.fakes.FakeGetSubscriptions
import proton.android.pass.uitest.fakes.FakeObserveSessionState
import proton.android.pass.uitest.fakes.FakePurchaseProduct
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ScreenUseCaseModule::class, PaymentsModule::class]
)
object PaymentTestModule {

    @Provides
    @Singleton
    fun provideFakeGetProducts(): FakeGetProducts = FakeGetProducts()

    @Provides
    @Singleton
    fun provideGetProducts(fake: FakeGetProducts): GetProducts = fake

    @Provides
    @Singleton
    fun provideFakeGetSubscriptions(): FakeGetSubscriptions = FakeGetSubscriptions()

    @Provides
    @Singleton
    fun provideGetSubscriptions(fake: FakeGetSubscriptions): GetSubscriptions = fake

    @Provides
    @Singleton
    fun provideFakeObserveSessionState(): FakeObserveSessionState = FakeObserveSessionState()

    @Provides
    @Singleton
    fun provideObserveSessionState(fake: FakeObserveSessionState): ObserveSessionState = fake

    @Provides
    @Singleton
    fun provideFakePurchaseProduct(): FakePurchaseProduct = FakePurchaseProduct()

    @Provides
    @Singleton
    fun providePurchaseProduct(fake: FakePurchaseProduct): PurchaseProduct = fake

    @Provides
    @Singleton
    fun provideFakePayments(): FakePayments = FakePayments().apply {
        productsResult = Result.success(
            listOf(
                paymentProduct(
                    id = "pass_plus_12",
                    amount = 2_990_000,
                    formattedAmount = "USD 2.99"
                ),
                paymentProduct(
                    id = "pass_unlimited_12",
                    amount = 9_990_000,
                    formattedAmount = "USD 9.99"
                )
            )
        )
    }

    @Provides
    @Singleton
    fun providePayments(fake: FakePayments): Payments = fake
}

private fun paymentProduct(
    id: String,
    amount: Long,
    formattedAmount: String
) = PaymentProduct(
    id = id,
    offers = listOf(
        PaymentOffer(
            token = "$id-token",
            isDiscounted = false,
            pricingPhases = listOf(
                PaymentPricingPhase(
                    amount = amount,
                    currency = "USD",
                    formattedAmount = formattedAmount,
                    period = "P1Y"
                )
            )
        )
    )
)
