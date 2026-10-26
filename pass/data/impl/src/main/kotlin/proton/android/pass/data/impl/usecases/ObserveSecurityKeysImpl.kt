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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.proton.core.usersettings.domain.usecase.ObserveRegisteredSecurityKeys
import proton.android.pass.data.api.usecases.ObserveSecurityKeys
import proton.android.pass.data.impl.account.toDomain
import proton.android.pass.domain.UserId
import proton.android.pass.domain.account.SecurityKey
import javax.inject.Inject

class ObserveSecurityKeysImpl @Inject constructor(
    private val observeRegisteredSecurityKeys: ObserveRegisteredSecurityKeys
) : ObserveSecurityKeys {

    override fun invoke(userId: UserId): Flow<List<SecurityKey>> =
        observeRegisteredSecurityKeys(userId).map { keys -> keys.map { it.toDomain() } }
}
