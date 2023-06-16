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
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ShareRepository
import proton.android.pass.data.api.usecases.CreateVault
import proton.pass.domain.Share
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

class CreateVaultImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val shareRepository: ShareRepository
) : CreateVault {

    override suspend fun invoke(userId: SessionUserId?, vault: NewVault): Share =
        if (userId == null) {
            val primaryUserId = accountManager.getPrimaryUserId().firstOrNull()
            if (primaryUserId != null) {
                createVault(primaryUserId, vault)
            } else {
                throw UserIdNotAvailableError()
            }
        } else {
            createVault(userId, vault)
        }


    private suspend fun createVault(userId: UserId, vault: NewVault): Share =
        shareRepository.createVault(userId, vault)
}
