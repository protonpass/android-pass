/*
 * Copyright (c) 2023 Proton AG
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

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.data.api.repositories.InviteRepository
import proton.android.pass.data.api.usecases.AcceptInvite
import proton.android.pass.data.api.usecases.ApplyPendingEvents
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.InviteToken
import javax.inject.Inject

class AcceptInviteImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val inviteRepository: InviteRepository,
    private val applyPendingEvents: ApplyPendingEvents
) : AcceptInvite {
    override suspend fun invoke(invite: InviteToken) {
        val userId = accountManager.getPrimaryUserId().filterNotNull().first()
        inviteRepository.acceptInvite(userId, invite)
        runCatching { applyPendingEvents() }
            .onSuccess {
                PassLogger.d(TAG, "Sync performed after accepting invite")
            }
            .onFailure {
                PassLogger.w(TAG, it, "Error performing sync after accepting invite")
            }
    }

    companion object {
        private const val TAG = "AcceptInviteImpl"
    }
}
