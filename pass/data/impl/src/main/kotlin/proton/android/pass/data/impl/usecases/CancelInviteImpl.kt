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
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.api.usecases.CancelInvite
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.domain.InviteId
import proton.android.pass.domain.NewUserInviteId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class CancelInviteImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val apiProvider: ApiProvider
) : CancelInvite {
    override suspend fun invoke(shareId: ShareId, inviteId: InviteId) {
        val userId = accountManager.getPrimaryUserId().filterNotNull().first()
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke {
                deleteInvite(
                    shareId = shareId.id,
                    inviteId = inviteId.value
                )
            }
            .valueOrThrow
    }

    override suspend fun invoke(shareId: ShareId, inviteId: NewUserInviteId) {
        val userId = accountManager.getPrimaryUserId().filterNotNull().first()
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke {
                deleteNewUserInvite(
                    shareId = shareId.id,
                    inviteId = inviteId.value
                )
            }
            .valueOrThrow
    }
}
