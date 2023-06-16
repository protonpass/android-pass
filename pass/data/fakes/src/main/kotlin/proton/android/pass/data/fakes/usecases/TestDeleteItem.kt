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

package proton.android.pass.data.fakes.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.usecases.DeleteItem
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class TestDeleteItem @Inject constructor() : DeleteItem {

    private var result: Result<Unit> = Result.success(Unit)
    private val memory: MutableList<Payload> = mutableListOf()

    fun setResult(value: Result<Unit>) {
        result = value
    }
    fun memory(): List<Payload> = memory

    override suspend fun invoke(userId: UserId?, shareId: ShareId, itemId: ItemId) {
        memory.add(Payload(userId, shareId, itemId))
        result.fold(
            onSuccess = {},
            onFailure = { throw it }
        )
    }

    data class Payload(
        val userId: UserId?,
        val shareId: ShareId,
        val itemId: ItemId
    )
}
