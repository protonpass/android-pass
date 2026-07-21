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

package proton.android.pass.data.impl.usecases.compromisedpassword

import kotlinx.coroutines.flow.firstOrNull
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.CompromisedPasswordRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.compromisedpassword.RefreshCompromisedPasswords
import proton.android.pass.domain.Item
import javax.inject.Inject

class RefreshCompromisedPasswordsImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val repository: CompromisedPasswordRepository
) : RefreshCompromisedPasswords {
    override suspend fun invoke(items: List<Item>) {
        val userId = observeCurrentUser().firstOrNull()?.userId ?: return
        repository.refresh(userId, items)
    }

    override suspend fun invoke(userId: UserId, items: List<Item>) = repository.refresh(userId, items)
}
