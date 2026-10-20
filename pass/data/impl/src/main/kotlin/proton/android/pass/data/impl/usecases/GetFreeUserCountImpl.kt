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

import kotlinx.coroutines.flow.first
import me.proton.core.user.domain.UserManager
import proton.android.pass.data.api.usecases.GetFreeUserCount
import proton.android.pass.data.api.usecases.ObserveReadyAccounts
import proton.android.pass.data.impl.account.toDomain
import javax.inject.Inject

class GetFreeUserCountImpl @Inject constructor(
    private val observeReadyAccounts: ObserveReadyAccounts,
    private val userManager: UserManager
) : GetFreeUserCount {

    override suspend fun invoke(): Int = observeReadyAccounts().first()
        .count { account -> userManager.getUser(account.userId).toDomain().isFree }

}
