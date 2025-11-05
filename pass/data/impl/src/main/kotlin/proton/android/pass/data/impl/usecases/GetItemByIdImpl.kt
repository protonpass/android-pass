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
import proton.android.pass.data.api.errors.UserIdNotAvailableError
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

class GetItemByIdImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val itemRepository: ItemRepository
) : GetItemById {

    override suspend fun invoke(shareId: ShareId, itemId: ItemId): Item {
        val user = observeCurrentUser().firstOrNull()
            ?: throw UserIdNotAvailableError()
        return itemRepository.getById(user.userId, shareId, itemId)
    }
}

