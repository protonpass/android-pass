/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package proton.android.pass.account.fakes.payment

import me.proton.core.payment.domain.entity.GooglePurchaseToken
import me.proton.core.payment.domain.entity.ProtonPaymentToken
import me.proton.core.payment.domain.repository.GooglePurchaseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGooglePurchaseRepository @Inject constructor() : GooglePurchaseRepository {
    override suspend fun deleteByGooglePurchaseToken(googlePurchaseToken: GooglePurchaseToken) = Unit

    override suspend fun deleteByProtonPaymentToken(paymentToken: ProtonPaymentToken) = Unit

    override suspend fun findGooglePurchaseToken(paymentToken: ProtonPaymentToken): GooglePurchaseToken? = null

    override suspend fun updateGooglePurchase(
        googlePurchaseToken: GooglePurchaseToken,
        paymentToken: ProtonPaymentToken
    ) = Unit
}
