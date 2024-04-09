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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.BreachRepository
import proton.android.pass.data.impl.remote.RemoteBreachDataSource
import proton.android.pass.domain.breach.BreachCustomEmail
import proton.android.pass.domain.breach.BreachCustomEmailId
import javax.inject.Inject

class BreachRepositoryImpl @Inject constructor(
    private val remote: RemoteBreachDataSource
) : BreachRepository {

    private val refreshFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private fun customEmailsFlow(userId: UserId) = refreshFlow
        .filter { it }
        .mapLatest { refreshEmails(userId) }
        .onEach {
            refreshFlow.update { false }
        }
        .distinctUntilChanged()

    override fun observeCustomEmails(userId: UserId): Flow<List<BreachCustomEmail>> = customEmailsFlow(userId)

    override suspend fun addCustomEmail(userId: UserId, email: String) {
        remote.addCustomEmail(userId, email)
        refreshFlow.update { true }
    }

    override suspend fun verifyCustomEmail(
        userId: UserId,
        emailId: BreachCustomEmailId,
        code: String
    ) {
        remote.verifyCustomEmail(userId, emailId, code)
        refreshFlow.update { true }
    }

    private suspend fun refreshEmails(userId: UserId): List<BreachCustomEmail> {
        val response = remote.getCustomEmails(userId)
        return response.emails.customEmails.map { it.toDomain() }
    }

    fun proton.android.pass.data.impl.responses.BreachCustomEmail.toDomain() = BreachCustomEmail(
        id = BreachCustomEmailId(customEmailId),
        email = email,
        verified = verified,
        breachCount = breachCounter
    )
}