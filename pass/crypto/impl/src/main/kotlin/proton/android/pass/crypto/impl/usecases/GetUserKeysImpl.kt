/*
 * Copyright (c) 2026 Proton AG
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


package proton.android.pass.crypto.impl.usecases

import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.user.domain.UserManager
import proton.android.pass.crypto.api.usecases.GetUserKeys
import proton.android.pass.crypto.api.usecases.UserKeys
import proton.android.pass.crypto.impl.CoreUserKeys
import proton.android.pass.domain.UserId
import javax.inject.Inject

class GetUserKeysImpl @Inject constructor(
    private val cryptoContext: CryptoContext,
    private val userManager: UserManager
) : GetUserKeys {

    override suspend fun invoke(userId: UserId): UserKeys = CoreUserKeys(
        user = userManager.getUser(userId),
        cryptoContext = cryptoContext
    )
}
