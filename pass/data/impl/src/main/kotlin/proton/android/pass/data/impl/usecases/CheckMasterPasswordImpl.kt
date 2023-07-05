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
import me.proton.core.crypto.common.keystore.PlainByteArray
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserManager
import proton.android.pass.data.api.usecases.CheckMasterPassword
import javax.inject.Inject

class CheckMasterPasswordImpl @Inject constructor(
    private val userManager: UserManager,
    private val accountManager: AccountManager
) : CheckMasterPassword {
    override suspend fun invoke(userId: UserId?, password: String): Boolean {
        val id = if (userId == null) {
            val primaryUserId = accountManager.getPrimaryUserId().first() ?: return false
            primaryUserId
        } else {
            userId
        }

        val res = userManager.unlockWithPassword(id, PlainByteArray(password.encodeToByteArray()))
        return when (res) {
            is UserManager.UnlockResult.Success -> true
            is UserManager.UnlockResult.Error -> false
        }
    }
}
