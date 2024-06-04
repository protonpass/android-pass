/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.data.impl.usecases.extrapassword

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.update
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordListener
import proton.android.pass.data.api.usecases.extrapassword.AuthWithExtraPasswordResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthWithExtraPasswordListenerImpl @Inject constructor() : AuthWithExtraPasswordListener {

    private val state: MutableStateFlow<Map<UserId, AuthWithExtraPasswordResult>> =
        MutableStateFlow(emptyMap())

    internal fun setState(userId: UserId, result: AuthWithExtraPasswordResult) {
        state.update {
            state.value + (userId to result)
        }
    }

    override suspend fun clearUserId(userId: UserId) {
        state.update {
            state.value - userId
        }
    }

    override suspend fun onAuthWithExtraPassword(userId: UserId): AuthWithExtraPasswordResult =
        state.mapLatest { it[userId] }.filterNotNull().first()
}
