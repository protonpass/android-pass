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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.repositories.CompromisedPasswordItem
import proton.android.pass.data.api.repositories.CompromisedPasswordRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.compromisedpassword.ObserveCompromisedPasswords
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class ObserveCompromisedPasswordsImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val repository: CompromisedPasswordRepository
) : ObserveCompromisedPasswords {
    override fun invoke(): Flow<List<CompromisedPasswordItem>> = observeCurrentUser().flatMapLatest { user ->
        repository.observeCompromisedItems(user.userId)
    }

    override fun invoke(userId: UserId): Flow<List<CompromisedPasswordItem>> =
        repository.observeCompromisedItems(userId)

    override fun invoke(
        userId: UserId,
        shareId: ShareId,
        itemId: ItemId
    ): Flow<Boolean> = repository.observeIsItemCompromised(userId, shareId, itemId)
}
