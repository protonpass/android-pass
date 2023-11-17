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

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.SessionUserId
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.UpdateVault
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.entity.NewVault
import javax.inject.Inject

class UpdateVaultImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : UpdateVault {
    override suspend fun invoke(userId: SessionUserId?, shareId: ShareId, vault: NewVault): Share =
        if (userId == null) {
            val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
            if (primaryUserId != null) {
                performUpdate(primaryUserId, shareId, vault)
            } else {
                throw UserIdNotAvailableError()
            }
        } else {
            performUpdate(userId, shareId, vault)
        }

    private suspend fun performUpdate(
        userId: SessionUserId,
        shareId: ShareId,
        vault: NewVault
    ): Share = shareRepository.updateVault(userId, shareId, vault)
}
