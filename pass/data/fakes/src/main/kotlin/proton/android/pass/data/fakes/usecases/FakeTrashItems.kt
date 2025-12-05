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
import proton.android.pass.data.api.usecases.TrashItems
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeTrashItems @Inject constructor() : TrashItems {

    private var result: Result<Unit> = Result.failure(IllegalStateException("FakeTrashItem.result not set"))

    private val memory = mutableListOf<Payload>()
    fun getMemory(): List<Payload> = memory

    fun setResult(result: Result<Unit>) {
        this.result = result
    }

    data class Payload(
        val userId: UserId?,
        val items: Map<ShareId, List<ItemId>>
    )

    override suspend fun invoke(userId: UserId?, items: Map<ShareId, List<ItemId>>) {
        memory.add(Payload(userId, items))
        result.getOrThrow()
    }
}
