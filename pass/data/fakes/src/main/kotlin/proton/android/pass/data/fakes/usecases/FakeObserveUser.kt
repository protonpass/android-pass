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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.ObserveUser
import proton.android.pass.domain.account.User
import proton.android.pass.test.domain.UserTestFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveUser @Inject constructor() : ObserveUser {

    private val userFlows = mutableMapOf<UserId, MutableStateFlow<User?>>()

    fun emitUser(userId: UserId, user: User?) {
        flowFor(userId).tryEmit(user)
    }

    override fun invoke(userId: UserId): Flow<User?> = flowFor(userId)

    private fun flowFor(userId: UserId) = userFlows.getOrPut(userId) {
        MutableStateFlow(createDefaultUser(userId))
    }

    companion object {
        const val USER_ID = "FakeObserveUser-DefaultUserId"
        const val EMAIL = "DefaultEmail"

        fun createDefaultUser(userId: UserId): User = UserTestFactory.create(
            email = EMAIL,
            name = "name",
            userId = userId
        )
    }

}
