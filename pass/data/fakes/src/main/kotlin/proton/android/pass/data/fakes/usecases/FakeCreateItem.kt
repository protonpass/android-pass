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
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ShareId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeCreateItem @Inject constructor() : CreateItem {

    private var item: Result<Item> = Result.failure(IllegalStateException("Result not set"))

    private val memory = mutableListOf<Payload>()

    fun hasBeenInvoked() = memory.isNotEmpty()
    fun memory(): List<Payload> = memory

    override suspend fun invoke(
        userId: UserId?,
        shareId: ShareId,
        itemContents: ItemContents
    ): Item {
        memory.add(Payload(userId ?: UserId(USER_ID), shareId, itemContents))
        return item.getOrThrow()
    }

    fun sendItem(item: Result<Item>) {
        this.item = item
    }

    data class Payload(
        val userId: UserId,
        val shareId: ShareId,
        val itemContents: ItemContents
    )

    companion object {
        const val USER_ID = "user_id"
    }
}
