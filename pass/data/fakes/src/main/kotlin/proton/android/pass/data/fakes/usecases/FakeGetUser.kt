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

package proton.android.pass.data.fakes.usecases

import proton.android.pass.data.api.usecases.GetUser
import proton.android.pass.domain.UserId
import proton.android.pass.domain.account.User
import proton.android.pass.test.domain.UserTestFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGetUser @Inject constructor() : GetUser {

    private val results = mutableMapOf<UserId, User>()

    fun setResult(userId: UserId, value: User) {
        results[userId] = value
    }

    override suspend fun invoke(userId: UserId): User = results[userId]
        ?: UserTestFactory.create(email = DEFAULT_EMAIL, userId = userId)

    companion object {
        private const val DEFAULT_EMAIL = "DefaultEmail"
    }

}
