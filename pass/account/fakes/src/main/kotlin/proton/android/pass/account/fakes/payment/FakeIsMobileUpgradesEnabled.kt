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

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.features.IsMobileUpgradesEnabled
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeIsMobileUpgradesEnabled @Inject constructor() : IsMobileUpgradesEnabled {
    override fun invoke(userId: UserId?): Boolean = false

    override fun isLocalEnabled(): Boolean = false

    override fun isRemoteEnabled(userId: UserId?): Boolean = false
}
