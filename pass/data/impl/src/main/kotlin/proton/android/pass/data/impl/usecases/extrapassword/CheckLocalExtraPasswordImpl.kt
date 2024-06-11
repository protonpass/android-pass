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

import kotlinx.coroutines.flow.first
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.crypto.common.keystore.EncryptedString
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.extrapassword.CheckLocalExtraPassword
import proton.android.pass.data.impl.repositories.ExtraPasswordRepository
import javax.inject.Inject

class CheckLocalExtraPasswordImpl @Inject constructor(
    private val accountManager: AccountManager,
    private val repository: ExtraPasswordRepository
) : CheckLocalExtraPassword {

    override suspend fun invoke(userId: UserId?, password: EncryptedString): Boolean {
        val actualUserID = userId ?: accountManager.getPrimaryUserId().first()
            ?: throw IllegalStateException("No user id found")
        return repository.checkAccessKeyForUser(actualUserID, password)
    }
}
