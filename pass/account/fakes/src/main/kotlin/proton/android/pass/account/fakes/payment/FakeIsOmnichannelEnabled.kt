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

package proton.android.pass.account.fakes.payment

import me.proton.core.domain.entity.UserId
import me.proton.core.payment.domain.features.IsOmnichannelEnabled
import javax.inject.Inject

@Suppress("NotImplementedDeclaration")
class FakeIsOmnichannelEnabled @Inject constructor() : IsOmnichannelEnabled {
    override fun invoke(userId: UserId?): Boolean {
        TODO("Not yet implemented")
    }

    override fun isLocalEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isRemoteEnabled(userId: UserId?): Boolean {
        TODO("Not yet implemented")
    }
}
