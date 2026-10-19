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
package proton.android.pass.test.domain

import proton.android.pass.account.fakes.FakeAccountManager
import proton.android.pass.domain.UserId
import proton.android.pass.domain.account.Account
import proton.android.pass.domain.account.AccountState

object AccountTestFactory {

    val DEFAULT_ACCOUNT = create(UserId(FakeAccountManager.USER_ID))

    fun create(userId: UserId) = Account(
        userId = userId,
        username = null,
        state = AccountState.Ready
    )
}
