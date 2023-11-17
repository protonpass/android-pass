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

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.network.data.ApiProvider
import proton.android.pass.data.api.usecases.TransferVaultOwnership
import proton.android.pass.data.impl.api.PasswordManagerApi
import proton.android.pass.data.impl.local.LocalShareDataSource
import proton.android.pass.data.impl.requests.TransferVaultOwnershipRequest
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class TransferVaultOwnershipImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val apiProvider: ApiProvider,
    private val localShareDataSource: LocalShareDataSource
) : TransferVaultOwnership {
    override suspend fun invoke(shareId: ShareId, memberShareId: ShareId) {
        val userId = requireNotNull(accountManager.getPrimaryUserId().first())
        apiProvider.get<PasswordManagerApi>(userId)
            .invoke {
                transferVaultOwnership(
                    shareId = shareId.id,
                    request = TransferVaultOwnershipRequest(
                        newOwnerShareId = memberShareId.id
                    )
                )
            }
            .valueOrThrow

        localShareDataSource.updateOwnershipStatus(userId, shareId, false)
    }
}
