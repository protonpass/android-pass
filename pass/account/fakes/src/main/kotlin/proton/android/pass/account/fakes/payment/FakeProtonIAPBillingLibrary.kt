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

import me.proton.core.payment.domain.usecase.ProtonIAPBillingLibrary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeProtonIAPBillingLibrary @Inject constructor() : ProtonIAPBillingLibrary {
    override fun isAvailable(): Boolean = false
}
