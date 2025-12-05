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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.BreachRepository
import proton.android.pass.data.api.usecases.RefreshBreaches
import proton.android.pass.common.api.safeRunCatching
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.log.api.PassLogger
import javax.inject.Inject

class RefreshBreachesImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val breachRepository: BreachRepository
) : RefreshBreaches {

    override suspend fun invoke(userId: UserId?) {
        PassLogger.i(TAG, "Refreshing breaches for $userId")
        safeRunCatching {
            val resolvedUserId = userId
                ?: accountManager.getPrimaryUserId().firstOrNull()
                ?: throw UserIdNotAvailableError()
            breachRepository.refreshBreaches(resolvedUserId)
        }.onFailure { error ->
            PassLogger.w(TAG, "Error refreshing breaches for $userId")
            PassLogger.w(TAG, error)
            throw error
        }
        PassLogger.i(TAG, "Finished refreshing breaches for $userId")
    }

    private companion object {
        private const val TAG = "RefreshBreachesImpl"
    }
}

